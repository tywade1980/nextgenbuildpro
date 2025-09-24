import { 
  Category, 
  Trade, 
  Scope, 
  Assembly, 
  Task, 
  Material 
} from '../models/CatalogueSchema';
import { v4 as uuidv4 } from 'uuid';
import { 
  collection, 
  doc, 
  setDoc, 
  getDoc, 
  getDocs, 
  updateDoc,
  query,
  where,
  orderBy,
  limit
} from 'firebase/firestore';
import { firestore } from '../firebase';

export class CatalogueDataService {
  // Collection references
  private categoriesRef = collection(firestore, 'categories');
  private tradesRef = collection(firestore, 'trades');
  private scopesRef = collection(firestore, 'scopes');
  private assembliesRef = collection(firestore, 'assemblies');
  private tasksRef = collection(firestore, 'tasks');
  private materialsRef = collection(firestore, 'materials');
  
  // Create new category
  async createCategory(data: Omit<Category, 'id' | 'createdAt' | 'updatedAt'>): Promise<Category> {
    const now = new Date();
    const id = uuidv4();
    
    const category: Category = {
      id,
      ...data,
      createdAt: now,
      updatedAt: now
    };
    
    await setDoc(doc(this.categoriesRef, id), category);
    return category;
  }
  
  // Create new trade
  async createTrade(data: Omit<Trade, 'id' | 'createdAt' | 'updatedAt'>): Promise<Trade> {
    const now = new Date();
    const id = uuidv4();
    
    const trade: Trade = {
      id,
      ...data,
      createdAt: now,
      updatedAt: now
    };
    
    await setDoc(doc(this.tradesRef, id), trade);
    return trade;
  }
  
  // Create new scope - COMPLETED FROM INCOMPLETE VERSION
  async createScope(data: Omit<Scope, 'id' | 'createdAt' | 'updatedAt'>): Promise<Scope> {
    const now = new Date();
    const id = uuidv4();
    
    const scope: Scope = {
      id,
      ...data,
      createdAt: now,
      updatedAt: now
    };
    
    await setDoc(doc(this.scopesRef, id), scope);
    return scope;
  }
  
  // Create new assembly
  async createAssembly(data: Omit<Assembly, 'id' | 'createdAt' | 'updatedAt'>): Promise<Assembly> {
    const now = new Date();
    const id = uuidv4();
    
    const assembly: Assembly = {
      id,
      ...data,
      createdAt: now,
      updatedAt: now
    };
    
    await setDoc(doc(this.assembliesRef, id), assembly);
    return assembly;
  }
  
  // Create new task
  async createTask(data: Omit<Task, 'id' | 'createdAt' | 'updatedAt'>): Promise<Task> {
    const now = new Date();
    const id = uuidv4();
    
    const task: Task = {
      id,
      ...data,
      createdAt: now,
      updatedAt: now
    };
    
    await setDoc(doc(this.tasksRef, id), task);
    return task;
  }
  
  // Create new material
  async createMaterial(data: Omit<Material, 'id' | 'createdAt' | 'updatedAt'>): Promise<Material> {
    const now = new Date();
    const id = uuidv4();
    
    const material: Material = {
      id,
      ...data,
      createdAt: now,
      updatedAt: now
    };
    
    await setDoc(doc(this.materialsRef, id), material);
    return material;
  }
  
  // Read operations
  async getCategory(id: string): Promise<Category | null> {
    const docRef = doc(this.categoriesRef, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? docSnap.data() as Category : null;
  }
  
  async getTrade(id: string): Promise<Trade | null> {
    const docRef = doc(this.tradesRef, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? docSnap.data() as Trade : null;
  }
  
  async getScope(id: string): Promise<Scope | null> {
    const docRef = doc(this.scopesRef, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? docSnap.data() as Scope : null;
  }
  
  async getAssembly(id: string): Promise<Assembly | null> {
    const docRef = doc(this.assembliesRef, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? docSnap.data() as Assembly : null;
  }
  
  async getTask(id: string): Promise<Task | null> {
    const docRef = doc(this.tasksRef, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? docSnap.data() as Task : null;
  }
  
  async getMaterial(id: string): Promise<Material | null> {
    const docRef = doc(this.materialsRef, id);
    const docSnap = await getDoc(docRef);
    return docSnap.exists() ? docSnap.data() as Material : null;
  }
  
  // List operations
  async getCategories(): Promise<Category[]> {
    const q = query(this.categoriesRef, where('isActive', '==', true), orderBy('sequence'));
    const snapshot = await getDocs(q);
    return snapshot.docs.map((doc) => doc.data() as Category);
  }
  
  async getTradesByCategory(categoryId: string): Promise<Trade[]> {
    const q = query(
      this.tradesRef,
      where('categoryId', '==', categoryId),
      where('isActive', '==', true),
      orderBy('sequence')
    );
    const snapshot = await getDocs(q);
    return snapshot.docs.map((doc) => doc.data() as Trade);
  }
  
  async getScopesByTrade(tradeId: string): Promise<Scope[]> {
    const q = query(
      this.scopesRef,
      where('tradeId', '==', tradeId),
      where('isActive', '==', true),
      orderBy('sequence')
    );
    const snapshot = await getDocs(q);
    return snapshot.docs.map((doc) => doc.data() as Scope);
  }
  
  async getAssembliesByScope(scopeId: string): Promise<Assembly[]> {
    const q = query(
      this.assembliesRef,
      where('scopeId', '==', scopeId),
      where('isActive', '==', true),
      orderBy('sequence')
    );
    const snapshot = await getDocs(q);
    return snapshot.docs.map((doc) => doc.data() as Assembly);
  }
  
  async getTasksByAssembly(assemblyId: string): Promise<Task[]> {
    const q = query(
      this.tasksRef,
      where('assemblyId', '==', assemblyId),
      where('isActive', '==', true),
      orderBy('sequence')
    );
    const snapshot = await getDocs(q);
    return snapshot.docs.map((doc) => doc.data() as Task);
  }
  
  async getMaterialsByAssembly(assemblyId: string): Promise<Material[]> {
    const q = query(
      this.materialsRef,
      where('assemblyId', '==', assemblyId),
      where('isActive', '==', true)
    );
    const snapshot = await getDocs(q);
    return snapshot.docs.map((doc) => doc.data() as Material);
  }
  
  // Update operations
  async updateCategory(id: string, updates: Partial<Omit<Category, 'id' | 'createdAt'>>): Promise<void> {
    const updateData = {
      ...updates,
      updatedAt: new Date()
    };
    const docRef = doc(this.categoriesRef, id);
    await updateDoc(docRef, updateData);
  }
  
  async updateTrade(id: string, updates: Partial<Omit<Trade, 'id' | 'createdAt'>>): Promise<void> {
    const updateData = {
      ...updates,
      updatedAt: new Date()
    };
    const docRef = doc(this.tradesRef, id);
    await updateDoc(docRef, updateData);
  }
  
  async updateScope(id: string, updates: Partial<Omit<Scope, 'id' | 'createdAt'>>): Promise<void> {
    const updateData = {
      ...updates,
      updatedAt: new Date()
    };
    const docRef = doc(this.scopesRef, id);
    await updateDoc(docRef, updateData);
  }
  
  async updateAssembly(id: string, updates: Partial<Omit<Assembly, 'id' | 'createdAt'>>): Promise<void> {
    const updateData = {
      ...updates,
      updatedAt: new Date()
    };
    const docRef = doc(this.assembliesRef, id);
    await updateDoc(docRef, updateData);
  }
  
  async updateTask(id: string, updates: Partial<Omit<Task, 'id' | 'createdAt'>>): Promise<void> {
    const updateData = {
      ...updates,
      updatedAt: new Date()
    };
    const docRef = doc(this.tasksRef, id);
    await updateDoc(docRef, updateData);
  }
  
  async updateMaterial(id: string, updates: Partial<Omit<Material, 'id' | 'createdAt'>>): Promise<void> {
    const updateData = {
      ...updates,
      updatedAt: new Date()
    };
    const docRef = doc(this.materialsRef, id);
    await updateDoc(docRef, updateData);
  }
  
  // Delete operations (soft delete by setting isActive to false)
  async deleteCategory(id: string): Promise<void> {
    await this.updateCategory(id, { isActive: false });
  }
  
  async deleteTrade(id: string): Promise<void> {
    await this.updateTrade(id, { isActive: false });
  }
  
  async deleteScope(id: string): Promise<void> {
    await this.updateScope(id, { isActive: false });
  }
  
  async deleteAssembly(id: string): Promise<void> {
    await this.updateAssembly(id, { isActive: false });
  }
  
  async deleteTask(id: string): Promise<void> {
    await this.updateTask(id, { isActive: false });
  }
  
  async deleteMaterial(id: string): Promise<void> {
    await this.updateMaterial(id, { isActive: false });
  }
  
  // Search operations
  async searchAssemblies(searchQuery: string, searchLimit: number = 50): Promise<Assembly[]> {
    // Note: Firestore has limited text search. In production, you might want to use Algolia or similar
    const q = query(
      this.assembliesRef,
      where('isActive', '==', true),
      limit(searchLimit)
    );
    const snapshot = await getDocs(q);
    
    const assemblies = snapshot.docs.map((doc) => doc.data() as Assembly);
    
    // Client-side filtering for name and description
    const lowerQuery = searchQuery.toLowerCase();
    return assemblies.filter((assembly: Assembly) => 
      assembly.name.toLowerCase().includes(lowerQuery) ||
      assembly.description.toLowerCase().includes(lowerQuery) ||
      assembly.tags.some((tag: string) => tag.toLowerCase().includes(lowerQuery))
    );
  }
  
  // Batch operations for complex hierarchical data
  async createCompleteAssembly(
    scopeId: string,
    assemblyData: Omit<Assembly, 'id' | 'createdAt' | 'updatedAt'>,
    tasks: Omit<Task, 'id' | 'assemblyId' | 'createdAt' | 'updatedAt'>[],
    materials: Omit<Material, 'id' | 'assemblyId' | 'createdAt' | 'updatedAt'>[]
  ): Promise<{ assembly: Assembly; tasks: Task[]; materials: Material[] }> {
    const assembly = await this.createAssembly({ ...assemblyData, scopeId });
    
    const createdTasks = await Promise.all(
      tasks.map(taskData => this.createTask({ ...taskData, assemblyId: assembly.id }))
    );
    
    const createdMaterials = await Promise.all(
      materials.map(materialData => this.createMaterial({ ...materialData, assemblyId: assembly.id }))
    );
    
    return {
      assembly,
      tasks: createdTasks,
      materials: createdMaterials
    };
  }
}