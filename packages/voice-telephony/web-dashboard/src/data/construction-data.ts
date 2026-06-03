// Construction market training data
export const constructionMarketData = [
  {
    region: 'Southeast US',
    projectType: 'residential',
    averageCostPerSqFt: 125.50,
    laborCostPercentage: 35,
    materialCostPercentage: 45,
    permitCostAverage: 2500,
    timelineAverage: 120,
    dataSource: 'Construction Industry Institute',
    collectionDate: '2024-01-15'
  },
  {
    region: 'Northeast US',
    projectType: 'residential',
    averageCostPerSqFt: 185.75,
    laborCostPercentage: 40,
    materialCostPercentage: 40,
    permitCostAverage: 4200,
    timelineAverage: 140,
    dataSource: 'Regional Construction Board',
    collectionDate: '2024-01-15'
  },
  {
    region: 'West Coast US',
    projectType: 'residential',
    averageCostPerSqFt: 220.25,
    laborCostPercentage: 45,
    materialCostPercentage: 35,
    permitCostAverage: 6500,
    timelineAverage: 160,
    dataSource: 'California Construction Association',
    collectionDate: '2024-01-15'
  },
  {
    region: 'Midwest US',
    projectType: 'commercial',
    averageCostPerSqFt: 95.30,
    laborCostPercentage: 30,
    materialCostPercentage: 50,
    permitCostAverage: 8500,
    timelineAverage: 180,
    dataSource: 'Midwest Construction Alliance',
    collectionDate: '2024-01-15'
  },
  {
    region: 'Southeast US',
    projectType: 'industrial',
    averageCostPerSqFt: 75.80,
    laborCostPercentage: 25,
    materialCostPercentage: 55,
    permitCostAverage: 15000,
    timelineAverage: 300,
    dataSource: 'Industrial Construction Report',
    collectionDate: '2024-01-15'
  }
];

// Labor rates data
export const laborRatesData = [
  {
    skillType: 'electrician',
    level: 'apprentice',
    hourlyRate: 18.50,
    overtimeRate: 27.75,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'electrician',
    level: 'journeyman',
    hourlyRate: 28.75,
    overtimeRate: 43.13,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'electrician',
    level: 'master',
    hourlyRate: 42.50,
    overtimeRate: 63.75,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'plumber',
    level: 'apprentice',
    hourlyRate: 16.25,
    overtimeRate: 24.38,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'plumber',
    level: 'journeyman',
    hourlyRate: 26.50,
    overtimeRate: 39.75,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'carpenter',
    level: 'apprentice',
    hourlyRate: 15.75,
    overtimeRate: 23.63,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'carpenter',
    level: 'journeyman',
    hourlyRate: 24.25,
    overtimeRate: 36.38,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'hvac',
    level: 'journeyman',
    hourlyRate: 29.50,
    overtimeRate: 44.25,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'general_labor',
    level: 'apprentice',
    hourlyRate: 14.50,
    overtimeRate: 21.75,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  },
  {
    skillType: 'project_manager',
    level: 'supervisor',
    hourlyRate: 45.00,
    overtimeRate: 67.50,
    region: 'Southeast US',
    effectiveDate: '2024-01-01'
  }
];

// Building codes data
export const buildingCodesData = [
  {
    codeNumber: 'IRC-2021-R302',
    title: 'Fire Separation Requirements',
    description: 'Establishes minimum fire separation requirements between dwelling units and garages',
    category: 'fire_safety',
    jurisdiction: 'state',
    effectiveDate: '2021-01-01',
    requirements: {
      walls: 'Minimum 1/2 inch gypsum board',
      doors: 'Self-closing, solid wood or fire-rated',
      penetrations: 'All openings must be fire-stopped'
    },
    penalties: {
      violation: '$500-2000 fine',
      correction_period: '30 days'
    }
  },
  {
    codeNumber: 'IRC-2021-E3501',
    title: 'Electrical Service Requirements',
    description: 'Minimum electrical service requirements for dwelling units',
    category: 'electrical',
    jurisdiction: 'state',
    effectiveDate: '2021-01-01',
    requirements: {
      service_size: 'Minimum 100 amperes',
      grounding: 'Equipment grounding conductor required',
      gfci: 'GFCI protection for bathrooms, kitchens, garages'
    },
    penalties: {
      violation: '$200-1000 fine',
      correction_period: '14 days'
    }
  },
  {
    codeNumber: 'IRC-2021-P2801',
    title: 'Plumbing Fixture Requirements',
    description: 'Minimum plumbing fixture requirements and clearances',
    category: 'plumbing',
    jurisdiction: 'state',
    effectiveDate: '2021-01-01',
    requirements: {
      water_closet_clearance: 'Minimum 15 inches from centerline to wall',
      fixture_supply: 'Individual shut-off valves required',
      venting: 'Proper venting required for all fixtures'
    },
    penalties: {
      violation: '$300-1500 fine',
      correction_period: '21 days'
    }
  },
  {
    codeNumber: 'ADA-2010-206',
    title: 'Accessible Routes',
    description: 'Requirements for accessible routes in public accommodations',
    category: 'accessibility',
    jurisdiction: 'federal',
    effectiveDate: '2010-03-15',
    requirements: {
      width: 'Minimum 36 inches clear width',
      slope: 'Maximum 1:20 slope (5%)',
      surface: 'Stable, firm, and slip-resistant'
    },
    penalties: {
      violation: 'Civil rights violation',
      correction_period: 'Immediate compliance required'
    }
  },
  {
    codeNumber: 'IECC-2021-C405',
    title: 'Lighting Efficiency',
    description: 'Energy efficiency requirements for lighting systems',
    category: 'energy_efficiency',
    jurisdiction: 'state',
    effectiveDate: '2021-01-01',
    requirements: {
      controls: 'Automatic shutoff controls required',
      efficacy: 'Minimum 45 lumens per watt',
      power_density: 'Maximum lighting power density limits'
    },
    penalties: {
      violation: '$100-500 fine',
      correction_period: '30 days'
    }
  }
];

// Project types data with typical characteristics
export const projectTypesData = [
  {
    type: 'residential',
    subTypes: ['single_family', 'townhouse', 'condominium', 'apartment_complex'],
    typicalTimeline: {
      single_family: 120,
      townhouse: 90,
      condominium: 180,
      apartment_complex: 365
    },
    commonTasks: [
      'foundation', 'framing', 'roofing', 'electrical', 'plumbing', 
      'insulation', 'drywall', 'flooring', 'kitchen', 'bathrooms', 'exterior'
    ],
    requiredSkills: ['carpenter', 'electrician', 'plumber', 'roofer', 'general_labor'],
    averageSquareFootage: {
      single_family: 2200,
      townhouse: 1800,
      condominium: 1200,
      apartment_complex: 900
    }
  },
  {
    type: 'commercial',
    subTypes: ['office', 'retail', 'restaurant', 'warehouse', 'medical'],
    typicalTimeline: {
      office: 180,
      retail: 120,
      restaurant: 90,
      warehouse: 150,
      medical: 240
    },
    commonTasks: [
      'site_preparation', 'foundation', 'structural', 'mechanical', 'electrical',
      'plumbing', 'hvac', 'fire_safety', 'accessibility', 'finishes'
    ],
    requiredSkills: ['project_manager', 'engineer', 'electrician', 'plumber', 'hvac', 'carpenter'],
    averageSquareFootage: {
      office: 5000,
      retail: 3500,
      restaurant: 2500,
      warehouse: 15000,
      medical: 4000
    }
  },
  {
    type: 'industrial',
    subTypes: ['manufacturing', 'processing', 'storage', 'utility'],
    typicalTimeline: {
      manufacturing: 365,
      processing: 300,
      storage: 180,
      utility: 240
    },
    commonTasks: [
      'site_development', 'heavy_foundations', 'structural_steel', 'specialized_electrical',
      'process_piping', 'hvac_industrial', 'safety_systems', 'environmental_controls'
    ],
    requiredSkills: ['engineer', 'project_manager', 'electrician', 'hvac', 'general_labor'],
    averageSquareFootage: {
      manufacturing: 50000,
      processing: 25000,
      storage: 30000,
      utility: 10000
    }
  }
];

// Architectural data and standards
export const architecturalData = [
  {
    category: 'room_dimensions',
    standards: {
      bedroom: { min_width: 8, min_length: 10, min_area: 80 },
      bathroom: { min_width: 5, min_length: 7, min_area: 35 },
      kitchen: { min_width: 8, min_length: 8, min_area: 64 },
      living_room: { min_width: 12, min_length: 12, min_area: 144 }
    }
  },
  {
    category: 'ceiling_heights',
    standards: {
      residential: { min_height: 8, recommended_height: 9 },
      commercial: { min_height: 9, recommended_height: 10 },
      industrial: { min_height: 12, recommended_height: 16 }
    }
  },
  {
    category: 'door_dimensions',
    standards: {
      interior: { width: 30, height: 80 },
      exterior: { width: 36, height: 80 },
      commercial: { width: 36, height: 84 },
      accessibility: { width: 32, height: 80 }
    }
  },
  {
    category: 'window_standards',
    standards: {
      egress: { min_opening_width: 20, min_opening_height: 24, min_area: 5.7 },
      natural_light: { min_area_percentage: 10 },
      ventilation: { min_openable_percentage: 4 }
    }
  }
];

// Training prompts for AI models
export const trainingPrompts = [
  {
    category: 'cost_estimation',
    prompt: 'Estimate the cost for a kitchen renovation project',
    context: 'Kitchen size: 150 sq ft, mid-range finishes, includes new cabinets, countertops, appliances, flooring',
    expected_response: 'Based on current market data, a 150 sq ft kitchen renovation with mid-range finishes would cost approximately $25,000-35,000, including: Cabinets ($8,000-12,000), Countertops ($2,000-4,000), Appliances ($6,000-10,000), Flooring ($1,500-3,000), Labor ($7,500-6,000)'
  },
  {
    category: 'code_compliance',
    prompt: 'What building codes apply to bathroom renovation?',
    context: 'Master bathroom renovation, moving plumbing fixtures, new electrical',
    expected_response: 'Key building codes for bathroom renovation include: IRC plumbing requirements (P2801), electrical GFCI protection (E3501), ventilation requirements (M1507), accessibility considerations if applicable (ADA), and local permit requirements for plumbing and electrical work'
  },
  {
    category: 'timeline_estimation',
    prompt: 'How long should a deck construction project take?',
    context: '16x20 ft composite deck, attached to house, includes railings and stairs',
    expected_response: 'A 16x20 ft composite deck project typically takes 3-5 days with a 2-person crew: Day 1: Foundation/footings, Day 2: Frame construction, Day 3: Decking installation, Day 4-5: Railings, stairs, and finishing touches. Weather and permit approval can affect timeline'
  }
];