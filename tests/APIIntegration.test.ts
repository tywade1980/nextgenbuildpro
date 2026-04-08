/**
 * API Integration Tests
 *
 * Simulates REST API-style interactions with the catalogue service layer.
 * Tests the full request/response lifecycle for common construction
 * estimating workflows, equivalent to what would be tested with curl commands
 * against a live API server.
 *
 * Workflows covered:
 *   GET  /api/assemblies/search?q=:query
 *   POST /api/assemblies            (create with tasks + materials)
 *   GET  /api/assemblies/:id
 *   PUT  /api/assemblies/:id        (update costs)
 *   GET  /api/pricing/material?name=:name&unit=:unit
 *   GET  /api/pricing/labor?trade=:trade&location=:location
 *   GET  /api/pricing/labortime?task=:task&unit=:unit
 *   POST /api/estimates/generate    (full estimate generation)
 */

import { CatalogueDataService } from '../services/CatalogueDataService';
import { PricingWebSearchService } from '../services/PricingWebSearchService';
import { CataloguePricingEnhancer } from '../services/CataloguePricingEnhancer';
import { Assembly, Task, Material } from '../models/CatalogueSchema';

// ── Firebase mocks ──────────────────────────────────────────────────────────

const mockSetDoc = jest.fn().mockResolvedValue(undefined);
const mockGetDoc = jest.fn();
const mockGetDocs = jest.fn();
const mockUpdateDoc = jest.fn().mockResolvedValue(undefined);

jest.mock('firebase/firestore', () => ({
  collection: jest.fn((_db: unknown, name: string) => ({ _name: name })),
  doc: jest.fn((_ref: unknown, id: string) => ({ _id: id })),
  setDoc: (...args: unknown[]) => mockSetDoc(...args),
  getDoc: (...args: unknown[]) => mockGetDoc(...args),
  getDocs: (...args: unknown[]) => mockGetDocs(...args),
  updateDoc: (...args: unknown[]) => mockUpdateDoc(...args),
  query: jest.fn((...args: unknown[]) => args),
  where: jest.fn((...args: unknown[]) => args),
  orderBy: jest.fn((...args: unknown[]) => args),
  limit: jest.fn((...args: unknown[]) => args),
}));

jest.mock('../firebase', () => ({ firestore: {} }));

// ── API simulation layer ─────────────────────────────────────────────────────
// These thin wrappers mimic the JSON request/response shape of a REST API,
// making it easy to reason about the contract at each endpoint.

interface ApiResponse<T> {
  status: number;
  data: T | null;
  error?: string;
}

async function getAssemblySearch(
  service: CatalogueDataService,
  query: string
): Promise<ApiResponse<Assembly[]>> {
  try {
    const data = await service.searchAssemblies(query);
    return { status: 200, data };
  } catch {
    return { status: 500, data: null, error: 'Internal server error' };
  }
}

async function postCreateAssembly(
  service: CatalogueDataService,
  scopeId: string,
  assemblyData: Omit<Assembly, 'id' | 'createdAt' | 'updatedAt'>,
  tasks: Omit<Task, 'id' | 'assemblyId' | 'createdAt' | 'updatedAt'>[],
  materials: Omit<Material, 'id' | 'assemblyId' | 'createdAt' | 'updatedAt'>[]
): Promise<ApiResponse<{ assembly: Assembly; tasks: Task[]; materials: Material[] }>> {
  try {
    const data = await service.createCompleteAssembly(scopeId, assemblyData, tasks, materials);
    return { status: 201, data };
  } catch {
    return { status: 500, data: null, error: 'Internal server error' };
  }
}

async function getAssemblyById(
  service: CatalogueDataService,
  id: string
): Promise<ApiResponse<Assembly>> {
  try {
    const assembly = await service.getAssembly(id);
    if (!assembly) return { status: 404, data: null, error: 'Assembly not found' };
    return { status: 200, data: assembly };
  } catch {
    return { status: 500, data: null, error: 'Internal server error' };
  }
}

async function putUpdateAssembly(
  service: CatalogueDataService,
  id: string,
  updates: Partial<Assembly>
): Promise<ApiResponse<{ updated: boolean }>> {
  try {
    await service.updateAssembly(id, updates);
    return { status: 200, data: { updated: true } };
  } catch {
    return { status: 500, data: null, error: 'Internal server error' };
  }
}

async function getMaterialPricing(
  pricingService: PricingWebSearchService,
  name: string,
  unit: string
): Promise<ApiResponse<{ name: string; averagePrice: number; unit: string }>> {
  try {
    const result = await pricingService.searchMaterialPricing(name, unit);
    if (!result) return { status: 404, data: null, error: 'Pricing not found' };
    return {
      status: 200,
      data: { name: result.name, averagePrice: result.averagePrice, unit: result.unit },
    };
  } catch {
    return { status: 500, data: null, error: 'Internal server error' };
  }
}

async function getLaborRate(
  pricingService: PricingWebSearchService,
  trade: string,
  location = 'USA'
): Promise<ApiResponse<{ trade: string; hourlyRate: number; location: string }>> {
  try {
    const result = await pricingService.searchLaborRate(trade, location);
    if (!result) return { status: 404, data: null, error: 'Labor rate not found' };
    return {
      status: 200,
      data: { trade: result.trade, hourlyRate: result.hourlyRate, location: result.location! },
    };
  } catch {
    return { status: 500, data: null, error: 'Internal server error' };
  }
}

async function getLaborTimeEstimate(
  pricingService: PricingWebSearchService,
  task: string,
  unit: string
): Promise<ApiResponse<{ task: string; hours: number; unit: string }>> {
  try {
    const hours = await pricingService.searchLaborTimeEstimate(task, unit);
    if (hours === null) return { status: 404, data: null, error: 'Estimate not found' };
    return { status: 200, data: { task, hours, unit } };
  } catch {
    return { status: 500, data: null, error: 'Internal server error' };
  }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function makeQuerySnap<T>(items: T[]) {
  return { docs: items.map((d) => ({ data: () => d })), size: items.length };
}

function makeDocSnap<T>(data: T | null) {
  return { exists: () => data !== null, data: () => data };
}

// ── Tests ────────────────────────────────────────────────────────────────────

describe('API Integration - Assembly Search (GET /api/assemblies/search)', () => {
  let service: CatalogueDataService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new CatalogueDataService();
  });

  it('returns 200 with matching assemblies for a known keyword', async () => {
    const assemblies: Assembly[] = [
      {
        id: 'asm-1',
        scopeId: 'scope-1',
        name: 'Toilet Rough-In',
        description: 'Rough plumbing for toilet',
        sequence: 1,
        unit: 'each',
        laborHours: 3,
        materialCost: 150,
        laborCost: 210,
        equipmentCost: 0,
        subcontractorCost: 0,
        otherCost: 0,
        totalCost: 360,
        markupPercentage: 15,
        notes: '',
        tags: ['plumbing', 'bathroom'],
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ];

    mockGetDocs.mockResolvedValueOnce(makeQuerySnap(assemblies));

    const response = await getAssemblySearch(service, 'toilet');

    expect(response.status).toBe(200);
    expect(response.data).toHaveLength(1);
    expect(response.data![0].name).toContain('Toilet');
  });

  it('returns 200 with empty array when no assemblies match', async () => {
    mockGetDocs.mockResolvedValueOnce(makeQuerySnap([]));

    const response = await getAssemblySearch(service, 'nonexistent-xyz');

    expect(response.status).toBe(200);
    expect(response.data).toHaveLength(0);
  });

  it('returns 200 matching by tag', async () => {
    const assemblies: Assembly[] = [
      {
        id: 'asm-2',
        scopeId: 'scope-2',
        name: 'Electrical Panel Installation',
        description: 'Install main electrical panel',
        sequence: 1,
        unit: 'each',
        laborHours: 8,
        materialCost: 800,
        laborCost: 520,
        equipmentCost: 0,
        subcontractorCost: 0,
        otherCost: 0,
        totalCost: 1320,
        markupPercentage: 15,
        notes: '',
        tags: ['electrical', 'panel'],
        isActive: true,
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ];

    mockGetDocs.mockResolvedValueOnce(makeQuerySnap(assemblies));

    const response = await getAssemblySearch(service, 'electrical');

    expect(response.status).toBe(200);
    expect(response.data).toHaveLength(1);
    expect(response.data![0].tags).toContain('electrical');
  });
});

describe('API Integration - Create Assembly (POST /api/assemblies)', () => {
  let service: CatalogueDataService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new CatalogueDataService();
  });

  it('returns 201 with created assembly, tasks, and materials', async () => {
    const assemblyData: Omit<Assembly, 'id' | 'createdAt' | 'updatedAt'> = {
      scopeId: 'scope-1',
      name: 'Bathroom Rough-In',
      description: 'Complete bathroom rough-in plumbing',
      sequence: 1,
      unit: 'each',
      laborHours: 12,
      materialCost: 450,
      laborCost: 840,
      equipmentCost: 0,
      subcontractorCost: 0,
      otherCost: 0,
      totalCost: 1290,
      markupPercentage: 15,
      notes: 'Includes toilet, sink, and shower rough-in',
      tags: ['plumbing', 'bathroom'],
      isActive: true,
    };

    const tasks: Omit<Task, 'id' | 'assemblyId' | 'createdAt' | 'updatedAt'>[] = [
      {
        name: 'Install toilet flange',
        description: 'Set and glue toilet flange',
        sequence: 1,
        laborHours: 1,
        materialCost: 25,
        laborCost: 70,
        equipmentCost: 0,
        notes: '',
        isActive: true,
      },
      {
        name: 'Install drain lines',
        description: 'Run PVC drain lines',
        sequence: 2,
        laborHours: 4,
        materialCost: 150,
        laborCost: 280,
        equipmentCost: 0,
        notes: '',
        isActive: true,
      },
    ];

    const materials: Omit<Material, 'id' | 'assemblyId' | 'createdAt' | 'updatedAt'>[] = [
      {
        name: 'PVC pipe 4"',
        description: '4-inch PVC drain pipe',
        quantity: 20,
        unit: 'linear foot',
        unitCost: 3.5,
        totalCost: 70,
        waste: 10,
        isActive: true,
      },
    ];

    const response = await postCreateAssembly(service, 'scope-1', assemblyData, tasks, materials);

    expect(response.status).toBe(201);
    expect(response.data).not.toBeNull();
    expect(response.data!.assembly.name).toBe('Bathroom Rough-In');
    expect(response.data!.tasks).toHaveLength(2);
    expect(response.data!.materials).toHaveLength(1);
    // 1 assembly + 2 tasks + 1 material = 4 setDoc calls
    expect(mockSetDoc).toHaveBeenCalledTimes(4);
  });

  it('assigns generated IDs to all created entities', async () => {
    const assemblyData: Omit<Assembly, 'id' | 'createdAt' | 'updatedAt'> = {
      scopeId: 'scope-1',
      name: 'Simple Assembly',
      description: '',
      sequence: 1,
      unit: 'each',
      laborHours: 1,
      materialCost: 50,
      laborCost: 50,
      equipmentCost: 0,
      subcontractorCost: 0,
      otherCost: 0,
      totalCost: 100,
      markupPercentage: 10,
      notes: '',
      tags: [],
      isActive: true,
    };

    const response = await postCreateAssembly(service, 'scope-1', assemblyData, [], []);

    expect(response.data!.assembly.id).toBeTruthy();
    expect(typeof response.data!.assembly.id).toBe('string');
  });
});

describe('API Integration - Get Assembly (GET /api/assemblies/:id)', () => {
  let service: CatalogueDataService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new CatalogueDataService();
  });

  it('returns 200 with the assembly when it exists', async () => {
    const assembly: Assembly = {
      id: 'asm-exists',
      scopeId: 'scope-1',
      name: 'Existing Assembly',
      description: '',
      sequence: 1,
      unit: 'each',
      laborHours: 5,
      materialCost: 200,
      laborCost: 250,
      equipmentCost: 0,
      subcontractorCost: 0,
      otherCost: 0,
      totalCost: 450,
      markupPercentage: 15,
      notes: '',
      tags: [],
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    mockGetDoc.mockResolvedValueOnce(makeDocSnap(assembly));

    const response = await getAssemblyById(service, 'asm-exists');

    expect(response.status).toBe(200);
    expect(response.data!.id).toBe('asm-exists');
    expect(response.data!.name).toBe('Existing Assembly');
  });

  it('returns 404 when assembly does not exist', async () => {
    mockGetDoc.mockResolvedValueOnce(makeDocSnap(null));

    const response = await getAssemblyById(service, 'asm-missing');

    expect(response.status).toBe(404);
    expect(response.data).toBeNull();
    expect(response.error).toBe('Assembly not found');
  });
});

describe('API Integration - Update Assembly (PUT /api/assemblies/:id)', () => {
  let service: CatalogueDataService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new CatalogueDataService();
  });

  it('returns 200 and calls updateDoc with the new values', async () => {
    const response = await putUpdateAssembly(service, 'asm-1', {
      materialCost: 300,
      totalCost: 600,
    });

    expect(response.status).toBe(200);
    expect(response.data!.updated).toBe(true);
    expect(mockUpdateDoc).toHaveBeenCalledTimes(1);
    const updateArgs = mockUpdateDoc.mock.calls[0][1];
    expect(updateArgs.materialCost).toBe(300);
    expect(updateArgs.totalCost).toBe(600);
    expect(updateArgs.updatedAt).toBeInstanceOf(Date);
  });
});

describe('API Integration - Pricing Endpoints', () => {
  let pricingService: PricingWebSearchService;

  beforeEach(() => {
    pricingService = new PricingWebSearchService();
  });

  afterEach(() => {
    pricingService.clearCache();
  });

  describe('GET /api/pricing/material', () => {
    it('returns 200 with material pricing data for known material', async () => {
      const response = await getMaterialPricing(pricingService, 'concrete', 'cubic yard');

      expect(response.status).toBe(200);
      expect(response.data!.averagePrice).toBeGreaterThan(0);
      expect(response.data!.unit).toBe('cubic yard');
    });

    it('returns 404 for unknown material', async () => {
      const response = await getMaterialPricing(pricingService, 'fictional unobtanium xyz', 'each');

      expect(response.status).toBe(404);
      expect(response.error).toBe('Pricing not found');
    });
  });

  describe('GET /api/pricing/labor', () => {
    it('returns 200 with labor rate for known trade', async () => {
      const response = await getLaborRate(pricingService, 'plumber', 'California');

      expect(response.status).toBe(200);
      expect(response.data!.trade).toBe('plumber');
      expect(response.data!.hourlyRate).toBeGreaterThan(0);
      expect(response.data!.location).toBe('California');
    });

    it('returns 404 for unknown trade', async () => {
      const response = await getLaborRate(pricingService, 'spaceship pilot xyz');

      expect(response.status).toBe(404);
      expect(response.error).toBe('Labor rate not found');
    });

    it('uses USA as default location', async () => {
      const response = await getLaborRate(pricingService, 'electrician');

      expect(response.status).toBe(200);
      expect(response.data!.location).toBe('USA');
    });
  });

  describe('GET /api/pricing/labortime', () => {
    it('returns 200 with labor hours for known task', async () => {
      const response = await getLaborTimeEstimate(pricingService, 'install toilet', 'each');

      expect(response.status).toBe(200);
      expect(response.data!.hours).toBeGreaterThan(0);
      expect(response.data!.unit).toBe('each');
    });

    it('returns 404 for unknown task', async () => {
      const response = await getLaborTimeEstimate(pricingService, 'teleport materials xyz', 'each');

      expect(response.status).toBe(404);
      expect(response.error).toBe('Estimate not found');
    });
  });
});

describe('API Integration - Full Estimate Generation (POST /api/estimates/generate)', () => {
  let catalogueService: CatalogueDataService;
  let pricingService: PricingWebSearchService;
  let enhancer: CataloguePricingEnhancer;

  beforeEach(() => {
    jest.clearAllMocks();
    catalogueService = new CatalogueDataService();
    pricingService = new PricingWebSearchService();
    enhancer = new CataloguePricingEnhancer(catalogueService, pricingService);
  });

  afterEach(() => {
    pricingService.clearCache();
  });

  it('generates a complete bathroom estimate with current market pricing', async () => {
    // Step 1: Look up material pricing
    const toiletPricing = await getMaterialPricing(pricingService, 'toilet', 'each');
    expect(toiletPricing.status).toBe(200);

    // Step 2: Look up labor rate for plumber
    const plumberRate = await getLaborRate(pricingService, 'plumber');
    expect(plumberRate.status).toBe(200);

    // Step 3: Look up labor time for toilet install
    const toiletInstallTime = await getLaborTimeEstimate(
      pricingService,
      'install toilet',
      'each'
    );
    expect(toiletInstallTime.status).toBe(200);

    // Step 4: Calculate estimated cost
    const materialCost = toiletPricing.data!.averagePrice;
    const laborCost = plumberRate.data!.hourlyRate * toiletInstallTime.data!.hours;
    const totalCost = materialCost + laborCost;

    // Verify that the estimate is within reasonable bounds
    expect(totalCost).toBeGreaterThan(200); // $200 minimum (materials + labor)
    expect(totalCost).toBeLessThan(1000);   // $1000 maximum for a single toilet install

    // Step 5: Create the assembly with the computed costs
    const assemblyData: Omit<Assembly, 'id' | 'createdAt' | 'updatedAt'> = {
      scopeId: 'scope-bathroom',
      name: 'Toilet Installation',
      description: 'Supply and install toilet',
      sequence: 1,
      unit: 'each',
      laborHours: toiletInstallTime.data!.hours,
      materialCost,
      laborCost,
      equipmentCost: 0,
      subcontractorCost: 0,
      otherCost: 0,
      totalCost,
      markupPercentage: 15,
      notes: `Market pricing as of ${new Date().toISOString().split('T')[0]}`,
      tags: ['plumbing', 'bathroom', 'toilet'],
      isActive: true,
    };

    const createResponse = await postCreateAssembly(
      catalogueService,
      'scope-bathroom',
      assemblyData,
      [],
      []
    );

    expect(createResponse.status).toBe(201);
    expect(createResponse.data!.assembly.totalCost).toBeCloseTo(totalCost, 2);
    expect(createResponse.data!.assembly.laborHours).toBe(toiletInstallTime.data!.hours);
  });

  it('generates a kitchen framing estimate with carpenter rates', async () => {
    const carpenterRate = await getLaborRate(pricingService, 'carpenter');
    const lumberPricing = await getMaterialPricing(pricingService, '2x4 lumber', 'each');

    expect(carpenterRate.status).toBe(200);
    expect(lumberPricing.status).toBe(200);

    // 40 studs for a kitchen wall frame
    const lumberQty = 40;
    const estimatedLaborHours = 8;
    const materialCost = lumberQty * lumberPricing.data!.averagePrice;
    const laborCost = carpenterRate.data!.hourlyRate * estimatedLaborHours;
    const totalCost = materialCost + laborCost;

    expect(materialCost).toBeGreaterThan(100);
    expect(laborCost).toBeGreaterThan(200);
    expect(totalCost).toBeGreaterThan(300);
  });
});
