/**
 * Tests for CatalogueExportImport utility
 */

import { CatalogueExportImport, CatalogueExportData } from '../services/CatalogueExportImport';
import * as fs from 'fs';

// Mock Firebase functions
jest.mock('firebase/firestore', () => ({
  collection: jest.fn(),
  getDocs: jest.fn(),
  setDoc: jest.fn(),
  doc: jest.fn(),
  deleteDoc: jest.fn(),
  query: jest.fn(),
  where: jest.fn()
}));

jest.mock('../firebase', () => ({
  firestore: {}
}));

// Mock fs functions
jest.mock('fs', () => ({
  writeFileSync: jest.fn(),
  readFileSync: jest.fn()
}));

jest.mock('path', () => ({
  resolve: jest.fn((p: string) => p)
}));

describe('CatalogueExportImport', () => {
  let exportImport: CatalogueExportImport;
  let mockGetDocs: jest.Mock;
  let mockSetDoc: jest.Mock;
  let mockDeleteDoc: jest.Mock;
  let mockQuery: jest.Mock;
  let mockWhere: jest.Mock;
  
  beforeEach(() => {
    exportImport = new CatalogueExportImport();
    
    // Get mocked functions
    const { getDocs, setDoc, deleteDoc, query, where } = require('firebase/firestore');
    mockGetDocs = getDocs as jest.Mock;
    mockSetDoc = setDoc as jest.Mock;
    mockDeleteDoc = deleteDoc as jest.Mock;
    mockQuery = query as jest.Mock;
    mockWhere = where as jest.Mock;
    
    // Clear mocks
    jest.clearAllMocks();
  });

  describe('exportCatalogue', () => {
    it('should export catalogue data to JSON file', async () => {
      // Mock Firestore responses
      const mockSnapshot = {
        docs: [
          { data: () => ({ id: '1', name: 'Category 1', isActive: true }) },
          { data: () => ({ id: '2', name: 'Category 2', isActive: true }) }
        ]
      };
      
      mockGetDocs.mockResolvedValue(mockSnapshot);
      
      const outputPath = '/tmp/test-export.json';
      
      await exportImport.exportCatalogue(outputPath);
      
      // Verify fs.writeFileSync was called
      expect(fs.writeFileSync).toHaveBeenCalledWith(
        outputPath,
        expect.stringContaining('"categories"')
      );
      
      // Verify all collections were queried
      expect(mockGetDocs).toHaveBeenCalledTimes(6); // categories, trades, scopes, assemblies, tasks, materials
    });

    it('should handle export errors', async () => {
      mockGetDocs.mockRejectedValue(new Error('Firestore error'));
      
      await expect(exportImport.exportCatalogue('/tmp/test.json')).rejects.toThrow('Firestore error');
    });
  });

  describe('importCatalogue', () => {
    it('should import catalogue data from JSON file', async () => {
      const mockCatalogueData: CatalogueExportData = {
        categories: [{ 
          id: '1', 
          name: 'Test Category', 
          description: 'Test', 
          sequence: 1, 
          isActive: true,
          createdAt: new Date(),
          updatedAt: new Date()
        }],
        trades: [],
        scopes: [],
        assemblies: [],
        tasks: [],
        materials: [],
        exportDate: new Date()
      };
      
      (fs.readFileSync as jest.Mock).mockReturnValue(JSON.stringify(mockCatalogueData));
      mockSetDoc.mockResolvedValue(undefined);
      
      const inputPath = '/tmp/test-import.json';
      
      await exportImport.importCatalogue(inputPath, false);
      
      // Verify fs.readFileSync was called
      expect(fs.readFileSync).toHaveBeenCalledWith(inputPath, 'utf8');
      
      // Verify setDoc was called for the category
      expect(mockSetDoc).toHaveBeenCalledTimes(1);
      
      // Check that the second argument (the data) contains our expected properties
      const setDocCall = mockSetDoc.mock.calls[0];
      const categoryData = setDocCall[1];
      expect(categoryData).toMatchObject({
        id: '1',
        name: 'Test Category',
        description: 'Test',
        sequence: 1,
        isActive: true
      });
      expect(categoryData.createdAt).toBeInstanceOf(Date);
      expect(categoryData.updatedAt).toBeInstanceOf(Date);
    });

    it('should validate catalogue structure', async () => {
      const invalidData = { categories: [] }; // Missing required fields
      
      (fs.readFileSync as jest.Mock).mockReturnValue(JSON.stringify(invalidData));
      
      await expect(exportImport.importCatalogue('/tmp/invalid.json')).rejects.toThrow('Invalid catalogue file structure');
    });

    it('should clear existing data when requested', async () => {
      const mockCatalogueData: CatalogueExportData = {
        categories: [],
        trades: [],
        scopes: [],
        assemblies: [],
        tasks: [],
        materials: [],
        exportDate: new Date()
      };
      
      const mockSnapshot = {
        docs: [
          { id: 'doc1' },
          { id: 'doc2' }
        ]
      };
      
      (fs.readFileSync as jest.Mock).mockReturnValue(JSON.stringify(mockCatalogueData));
      mockGetDocs.mockResolvedValue(mockSnapshot);
      mockDeleteDoc.mockResolvedValue(undefined);
      
      await exportImport.importCatalogue('/tmp/test.json', true);
      
      // Verify deleteDoc was called for each document in each collection
      expect(mockDeleteDoc).toHaveBeenCalledTimes(12); // 2 docs × 6 collections
    });
  });

  describe('clearExistingData', () => {
    it('should delete all documents from all collections', async () => {
      const mockSnapshot = {
        docs: [
          { id: 'doc1' },
          { id: 'doc2' }
        ]
      };
      
      mockGetDocs.mockResolvedValue(mockSnapshot);
      mockDeleteDoc.mockResolvedValue(undefined);
      
      await exportImport.clearExistingData();
      
      // Verify getDocs was called for each collection
      expect(mockGetDocs).toHaveBeenCalledTimes(6);
      
      // Verify deleteDoc was called for each document in each collection
      expect(mockDeleteDoc).toHaveBeenCalledTimes(12); // 2 docs × 6 collections
    });
  });

  describe('exportActiveCatalogue', () => {
    it('should export only active catalogue data', async () => {
      const mockSnapshot = {
        docs: [
          { data: () => ({ id: '1', name: 'Active Item', isActive: true }) }
        ]
      };
      
      mockGetDocs.mockResolvedValue(mockSnapshot);
      
      await exportImport.exportActiveCatalogue('/tmp/active-export.json');
      
      // Verify query with where clause was used
      expect(mockQuery).toHaveBeenCalledTimes(6);
      expect(mockWhere).toHaveBeenCalledWith('isActive', '==', true);
      
      // Verify file was written
      expect(fs.writeFileSync).toHaveBeenCalled();
    });
  });

  describe('getCatalogueStats', () => {
    it('should return catalogue statistics', async () => {
      const mockAllSnapshot = { size: 10 };
      const mockActiveSnapshot = { size: 8 };
      
      mockGetDocs
        .mockResolvedValueOnce(mockAllSnapshot) // categories
        .mockResolvedValueOnce(mockAllSnapshot) // trades
        .mockResolvedValueOnce(mockAllSnapshot) // scopes
        .mockResolvedValueOnce(mockAllSnapshot) // assemblies
        .mockResolvedValueOnce(mockAllSnapshot) // tasks  
        .mockResolvedValueOnce(mockAllSnapshot) // materials
        .mockResolvedValueOnce(mockActiveSnapshot) // active categories
        .mockResolvedValueOnce(mockActiveSnapshot) // active trades
        .mockResolvedValueOnce(mockActiveSnapshot) // active scopes
        .mockResolvedValueOnce(mockActiveSnapshot) // active assemblies
        .mockResolvedValueOnce(mockActiveSnapshot) // active tasks
        .mockResolvedValueOnce(mockActiveSnapshot); // active materials
      
      const stats = await exportImport.getCatalogueStats();
      
      expect(stats).toEqual({
        categories: 10,
        trades: 10,
        scopes: 10,
        assemblies: 10,
        tasks: 10,
        materials: 10,
        activeCategories: 8,
        activeTrades: 8,
        activeScopes: 8,
        activeAssemblies: 8,
        activeTasks: 8,
        activeMaterials: 8
      });
      
      // Verify getDocs was called the correct number of times
      expect(mockGetDocs).toHaveBeenCalledTimes(12); // 6 for all + 6 for active
    });
  });
});