/**
 * Utility for exporting and importing catalogue data
 * TypeScript version of the JavaScript implementation provided
 */

import * as fs from 'fs';
import * as path from 'path';
import { 
  collection, 
  getDocs, 
  setDoc, 
  doc, 
  deleteDoc,
  query,
  where
} from 'firebase/firestore';
import { firestore } from '../firebase';
import { 
  Category, 
  Trade, 
  Scope, 
  Assembly, 
  Task, 
  Material 
} from '../models/CatalogueSchema';

export interface CatalogueExportData {
  categories: Category[];
  trades: Trade[];
  scopes: Scope[];
  assemblies: Assembly[];
  tasks: Task[];
  materials: Material[];
  exportDate: Date;
}

export class CatalogueExportImport {
  private readonly collectionsConfig = {
    categories: 'categories',
    trades: 'trades', 
    scopes: 'scopes',
    assemblies: 'assemblies',
    tasks: 'tasks',
    materials: 'materials'
  };

  /**
   * Export the entire catalogue to JSON file
   */
  async exportCatalogue(outputPath: string): Promise<void> {
    console.log('Exporting catalogue data...');
    
    try {
      // Get all categories
      const categoriesSnapshot = await getDocs(collection(firestore, this.collectionsConfig.categories));
      const categories = categoriesSnapshot.docs.map(doc => doc.data() as Category);
      
      // Get all trades
      const tradesSnapshot = await getDocs(collection(firestore, this.collectionsConfig.trades));
      const trades = tradesSnapshot.docs.map(doc => doc.data() as Trade);
      
      // Get all scopes
      const scopesSnapshot = await getDocs(collection(firestore, this.collectionsConfig.scopes));
      const scopes = scopesSnapshot.docs.map(doc => doc.data() as Scope);
      
      // Get all assemblies
      const assembliesSnapshot = await getDocs(collection(firestore, this.collectionsConfig.assemblies));
      const assemblies = assembliesSnapshot.docs.map(doc => doc.data() as Assembly);
      
      // Get all tasks
      const tasksSnapshot = await getDocs(collection(firestore, this.collectionsConfig.tasks));
      const tasks = tasksSnapshot.docs.map(doc => doc.data() as Task);
      
      // Get all materials
      const materialsSnapshot = await getDocs(collection(firestore, this.collectionsConfig.materials));
      const materials = materialsSnapshot.docs.map(doc => doc.data() as Material);
      
      // Create the complete catalogue object
      const catalogue: CatalogueExportData = {
        categories,
        trades,
        scopes,
        assemblies,
        tasks,
        materials,
        exportDate: new Date()
      };
      
      // Write to file
      fs.writeFileSync(
        path.resolve(outputPath), 
        JSON.stringify(catalogue, null, 2)
      );
      
      console.log(`Catalogue exported to ${outputPath}`);
    } catch (error) {
      console.error('Error exporting catalogue:', error);
      throw error;
    }
  }
  
  /**
   * Import catalogue from JSON file
   */
  async importCatalogue(inputPath: string, clearExisting: boolean = false): Promise<void> {
    console.log('Importing catalogue data...');
    
    try {
      // Read file
      const fileContent = fs.readFileSync(path.resolve(inputPath), 'utf8');
      const catalogue: CatalogueExportData = JSON.parse(fileContent);
      
      // Validate catalogue structure
      if (!catalogue.categories || !catalogue.trades || !catalogue.scopes || 
          !catalogue.assemblies || !catalogue.tasks || !catalogue.materials) {
        throw new Error('Invalid catalogue file structure');
      }
      
      // Clear existing data if requested
      if (clearExisting) {
        await this.clearExistingData();
      }
      
      // Import in batches to avoid overloading the database
      console.log('Importing categories...');
      for (const category of catalogue.categories) {
        await setDoc(doc(firestore, this.collectionsConfig.categories, category.id), {
          ...category,
          createdAt: category.createdAt instanceof Date ? category.createdAt : new Date(category.createdAt),
          updatedAt: category.updatedAt instanceof Date ? category.updatedAt : new Date(category.updatedAt)
        });
      }
      
      console.log('Importing trades...');
      for (const trade of catalogue.trades) {
        await setDoc(doc(firestore, this.collectionsConfig.trades, trade.id), {
          ...trade,
          createdAt: trade.createdAt instanceof Date ? trade.createdAt : new Date(trade.createdAt),
          updatedAt: trade.updatedAt instanceof Date ? trade.updatedAt : new Date(trade.updatedAt)
        });
      }
      
      console.log('Importing scopes...');
      for (const scope of catalogue.scopes) {
        await setDoc(doc(firestore, this.collectionsConfig.scopes, scope.id), {
          ...scope,
          createdAt: scope.createdAt instanceof Date ? scope.createdAt : new Date(scope.createdAt),
          updatedAt: scope.updatedAt instanceof Date ? scope.updatedAt : new Date(scope.updatedAt)
        });
      }
      
      console.log('Importing assemblies...');
      for (const assembly of catalogue.assemblies) {
        await setDoc(doc(firestore, this.collectionsConfig.assemblies, assembly.id), {
          ...assembly,
          createdAt: assembly.createdAt instanceof Date ? assembly.createdAt : new Date(assembly.createdAt),
          updatedAt: assembly.updatedAt instanceof Date ? assembly.updatedAt : new Date(assembly.updatedAt)
        });
      }
      
      console.log('Importing tasks...');
      for (const task of catalogue.tasks) {
        await setDoc(doc(firestore, this.collectionsConfig.tasks, task.id), {
          ...task,
          createdAt: task.createdAt instanceof Date ? task.createdAt : new Date(task.createdAt),
          updatedAt: task.updatedAt instanceof Date ? task.updatedAt : new Date(task.updatedAt)
        });
      }
      
      console.log('Importing materials...');
      for (const material of catalogue.materials) {
        await setDoc(doc(firestore, this.collectionsConfig.materials, material.id), {
          ...material,
          createdAt: material.createdAt instanceof Date ? material.createdAt : new Date(material.createdAt),
          updatedAt: material.updatedAt instanceof Date ? material.updatedAt : new Date(material.updatedAt)
        });
      }
      
      console.log('Catalogue import completed successfully');
    } catch (error) {
      console.error('Error importing catalogue:', error);
      throw error;
    }
  }
  
  /**
   * Clear all existing catalogue data
   */
  async clearExistingData(): Promise<void> {
    console.log('Clearing existing catalogue data...');
    
    const collections = [
      this.collectionsConfig.categories,
      this.collectionsConfig.trades,
      this.collectionsConfig.scopes,
      this.collectionsConfig.assemblies,
      this.collectionsConfig.tasks,
      this.collectionsConfig.materials
    ];
    
    for (const collectionName of collections) {
      const snapshot = await getDocs(collection(firestore, collectionName));
      
      for (const docSnapshot of snapshot.docs) {
        await deleteDoc(doc(firestore, collectionName, docSnapshot.id));
      }
    }
    
    console.log('Existing data cleared');
  }

  /**
   * Export only active catalogue data (where isActive = true)
   */
  async exportActiveCatalogue(outputPath: string): Promise<void> {
    console.log('Exporting active catalogue data...');
    
    try {
      // Get all active categories
      const categoriesQuery = query(collection(firestore, this.collectionsConfig.categories), where('isActive', '==', true));
      const categoriesSnapshot = await getDocs(categoriesQuery);
      const categories = categoriesSnapshot.docs.map(doc => doc.data() as Category);
      
      // Get all active trades
      const tradesQuery = query(collection(firestore, this.collectionsConfig.trades), where('isActive', '==', true));
      const tradesSnapshot = await getDocs(tradesQuery);
      const trades = tradesSnapshot.docs.map(doc => doc.data() as Trade);
      
      // Get all active scopes
      const scopesQuery = query(collection(firestore, this.collectionsConfig.scopes), where('isActive', '==', true));
      const scopesSnapshot = await getDocs(scopesQuery);
      const scopes = scopesSnapshot.docs.map(doc => doc.data() as Scope);
      
      // Get all active assemblies
      const assembliesQuery = query(collection(firestore, this.collectionsConfig.assemblies), where('isActive', '==', true));
      const assembliesSnapshot = await getDocs(assembliesQuery);
      const assemblies = assembliesSnapshot.docs.map(doc => doc.data() as Assembly);
      
      // Get all active tasks
      const tasksQuery = query(collection(firestore, this.collectionsConfig.tasks), where('isActive', '==', true));
      const tasksSnapshot = await getDocs(tasksQuery);
      const tasks = tasksSnapshot.docs.map(doc => doc.data() as Task);
      
      // Get all active materials
      const materialsQuery = query(collection(firestore, this.collectionsConfig.materials), where('isActive', '==', true));
      const materialsSnapshot = await getDocs(materialsQuery);
      const materials = materialsSnapshot.docs.map(doc => doc.data() as Material);
      
      // Create the complete catalogue object
      const catalogue: CatalogueExportData = {
        categories,
        trades,
        scopes,
        assemblies,
        tasks,
        materials,
        exportDate: new Date()
      };
      
      // Write to file
      fs.writeFileSync(
        path.resolve(outputPath), 
        JSON.stringify(catalogue, null, 2)
      );
      
      console.log(`Active catalogue exported to ${outputPath}`);
    } catch (error) {
      console.error('Error exporting active catalogue:', error);
      throw error;
    }
  }

  /**
   * Get catalogue export statistics without actually exporting
   */
  async getCatalogueStats(): Promise<{
    categories: number;
    trades: number;
    scopes: number;
    assemblies: number;
    tasks: number;
    materials: number;
    activeCategories: number;
    activeTrades: number;
    activeScopes: number;
    activeAssemblies: number;
    activeTasks: number;
    activeMaterials: number;
  }> {
    console.log('Getting catalogue statistics...');
    
    try {
      // Get all documents
      const [categoriesSnapshot, tradesSnapshot, scopesSnapshot, assembliesSnapshot, tasksSnapshot, materialsSnapshot] = await Promise.all([
        getDocs(collection(firestore, this.collectionsConfig.categories)),
        getDocs(collection(firestore, this.collectionsConfig.trades)),
        getDocs(collection(firestore, this.collectionsConfig.scopes)),
        getDocs(collection(firestore, this.collectionsConfig.assemblies)),
        getDocs(collection(firestore, this.collectionsConfig.tasks)),
        getDocs(collection(firestore, this.collectionsConfig.materials))
      ]);

      // Get active documents
      const [activeCategoriesSnapshot, activeTradesSnapshot, activeScopesSnapshot, activeAssembliesSnapshot, activeTasksSnapshot, activeMaterialsSnapshot] = await Promise.all([
        getDocs(query(collection(firestore, this.collectionsConfig.categories), where('isActive', '==', true))),
        getDocs(query(collection(firestore, this.collectionsConfig.trades), where('isActive', '==', true))),
        getDocs(query(collection(firestore, this.collectionsConfig.scopes), where('isActive', '==', true))),
        getDocs(query(collection(firestore, this.collectionsConfig.assemblies), where('isActive', '==', true))),
        getDocs(query(collection(firestore, this.collectionsConfig.tasks), where('isActive', '==', true))),
        getDocs(query(collection(firestore, this.collectionsConfig.materials), where('isActive', '==', true)))
      ]);

      return {
        categories: categoriesSnapshot.size,
        trades: tradesSnapshot.size,
        scopes: scopesSnapshot.size,
        assemblies: assembliesSnapshot.size,
        tasks: tasksSnapshot.size,
        materials: materialsSnapshot.size,
        activeCategories: activeCategoriesSnapshot.size,
        activeTrades: activeTradesSnapshot.size,
        activeScopes: activeScopesSnapshot.size,
        activeAssemblies: activeAssembliesSnapshot.size,
        activeTasks: activeTasksSnapshot.size,
        activeMaterials: activeMaterialsSnapshot.size
      };
    } catch (error) {
      console.error('Error getting catalogue statistics:', error);
      throw error;
    }
  }
}