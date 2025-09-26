import React, { useState, useEffect } from 'react';
import { 
  View, 
  ScrollView, 
  Text, 
  StyleSheet, 
  TouchableOpacity,
  TextInput,
  FlatList,
  Modal,
  ActivityIndicator,
  Alert
} from 'react-native';

const EstimateEditor = ({
  estimateId,
  templateId,
  clientId,
  projectId,
  onSave,
  onCancel
}) => {
  const [estimate, setEstimate] = useState(null);
  const [sections, setSections] = useState([]);
  const [clients, setClients] = useState([]);
  const [selectedClient, setSelectedClient] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showAssemblySelector, setShowAssemblySelector] = useState(false);
  const [currentSectionIndex, setCurrentSectionIndex] = useState(-1);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  
  // Load estimate data on component mount
  useEffect(() => {
    const loadData = async () => {
      setIsLoading(true);
      
      try {
        // Load clients
        const clientsData = await fetchClients();
        setClients(clientsData);
        
        if (estimateId) {
          // Load existing estimate
          const estimateData = await fetchEstimate(estimateId);
          setEstimate(estimateData);
          setSections(estimateData.sections || []);
          
          if (estimateData.clientId) {
            const client = clientsData.find((c) => c.id === estimateData.clientId);
            setSelectedClient(client || null);
          }
        } else if (templateId) {
          // Create new estimate from template
          const template = await fetchTemplate(templateId);
          
          const newEstimate = {
            id: 'new',
            title: `New Estimate - ${template.name}`,
            status: 'draft',
            clientId: clientId || '',
            projectId: projectId || '',
            issueDate: new Date(),
            expiryDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days from now
            taxRate: template.defaultTaxRate,
            markup: template.defaultMarkup,
            terms: template.termsAndConditions,
            notes: template.notes,
            customFields: {...template.customFields}
          };
          
          setEstimate(newEstimate);
          setSections(template.sections || []);
          
          if (clientId) {
            const client = clientsData.find((c) => c.id === clientId);
            setSelectedClient(client || null);
          }
        } else {
          // Create blank estimate
          const newEstimate = {
            id: 'new',
            title: 'New Estimate',
            status: 'draft',
            clientId: clientId || '',
            projectId: projectId || '',
            issueDate: new Date(),
            expiryDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000), // 30 days from now
            taxRate: 0,
            markup: 0,
            terms: '',
            notes: ''
          };
          
          setEstimate(newEstimate);
          setSections([
            {
              id: `section-${Date.now()}`,
              name: 'General',
              sequence: 1,
              items: []
            }
          ]);
          
          if (clientId) {
            const client = clientsData.find((c) => c.id === clientId);
            setSelectedClient(client || null);
          }
        }
      } catch (error) {
        console.error('Error loading estimate data:', error);
        Alert.alert('Error', 'Failed to load estimate data: ' + error.message);
      } finally {
        setIsLoading(false);
      }
    };
    
    loadData();
  }, [estimateId, templateId, clientId, projectId]);
  
  // Handle adding a new section
  const handleAddSection = () => {
    const newSection = {
      id: `section-${Date.now()}`,
      name: 'New Section',
      sequence: sections.length + 1,
      items: []
    };
    
    setSections([...sections, newSection]);
  };
  
  // Handle adding an item to a section
  const handleAddItem = (sectionIndex) => {
    setCurrentSectionIndex(sectionIndex);
    setShowAssemblySelector(true);
  };
  
  // Handle searching for assemblies
  const handleSearchAssemblies = async (query) => {
    setSearchQuery(query);
    
    if (query.length < 3) {
      setSearchResults([]);
      return;
    }
    
    try {
      const results = await searchAssemblies(query);
      setSearchResults(results);
    } catch (error) {
      console.error('Error searching assemblies:', error);
      setSearchResults([]);
    }
  };
  
  // Handle selecting an assembly to add
  const handleSelectAssembly = async (assembly) => {
    if (currentSectionIndex < 0 || currentSectionIndex >= sections.length) {
      return;
    }
    
    try {
      const lineItem = await convertAssemblyToLineItem(assembly, 1);
      
      const updatedSections = [...sections];
      updatedSections[currentSectionIndex].items.push(lineItem);
      
      setSections(updatedSections);
      setShowAssemblySelector(false);
      setSearchQuery('');
      setSearchResults([]);
    } catch (error) {
      console.error('Error adding assembly:', error);
      Alert.alert('Error', 'Failed to add assembly: ' + error.message);
    }
  };
  
  // Handle removing an item
  const handleRemoveItem = (sectionIndex, itemIndex) => {
    const updatedSections = [...sections];
    updatedSections[sectionIndex].items.splice(itemIndex, 1);
    
    setSections(updatedSections);
  };
  
  // Handle updating item quantity
  const handleUpdateQuantity = (sectionIndex, itemIndex, quantity) => {
    const updatedSections = [...sections];
    updatedSections[sectionIndex].items[itemIndex].quantity = quantity;
    
    // Recalculate totals
    const item = updatedSections[sectionIndex].items[itemIndex];
    const calculations = calculateLineItemTotals(item);
    
    updatedSections[sectionIndex].items[itemIndex].materialCost = calculations.materialCost;
    updatedSections[sectionIndex].items[itemIndex].laborCost = calculations.laborCost;
    updatedSections[sectionIndex].items[itemIndex].equipmentCost = calculations.equipmentCost;
    updatedSections[sectionIndex].items[itemIndex].subtotal = calculations.subtotal;
    updatedSections[sectionIndex].items[itemIndex].markupAmount = calculations.markupAmount;
    updatedSections[sectionIndex].items[itemIndex].taxAmount = calculations.taxAmount;
    updatedSections[sectionIndex].items[itemIndex].total = calculations.total;
    updatedSections[sectionIndex].items[itemIndex].laborHours = calculations.laborHours;
    
    setSections(updatedSections);
  };
  
  // Handle saving the estimate - COMPLETION OF THE TRUNCATED CODE FROM PROBLEM STATEMENT
  const handleSaveEstimate = async () => {
    if (!estimate || !selectedClient) {
      Alert.alert('Validation Error', 'Please select a client before saving.');
      return;
    }
    
    try {
      setIsLoading(true);
      
      const estimateData = {
        ...estimate,
        clientId: selectedClient.id,
        sections: sections
      };
      
      let savedEstimateId;
      
      if (estimate.id === 'new') {
        // Create new estimate
        const newEstimate = await createEstimate(estimateData);
        savedEstimateId = newEstimate.id;
      } else {
        // Update existing estimate
        await updateEstimate(estimate.id, estimateData);
        savedEstimateId = estimate.id;
      }
      
      // Call the onSave callback if provided
      if (onSave) {
        onSave(savedEstimateId, estimateData);
      }
      
      Alert.alert('Success', 'Estimate saved successfully!');
      
    } catch (error) {
      console.error('Error saving estimate:', error);
      Alert.alert('Error', 'Failed to save estimate: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };
  
  // Calculate line item totals
  const calculateLineItemTotals = (item) => {
    const baseQuantity = item.quantity || 0;
    const materialCost = (item.materialCostPerUnit || 0) * baseQuantity;
    const laborCost = (item.laborHours || 0) * (item.laborRate || 0) * baseQuantity;
    const equipmentCost = (item.equipmentCostPerUnit || 0) * baseQuantity;
    
    const subtotal = materialCost + laborCost + equipmentCost;
    const markupAmount = subtotal * ((item.markupPercentage || 0) / 100);
    const subtotalWithMarkup = subtotal + markupAmount;
    const taxAmount = subtotalWithMarkup * ((item.taxRate || 0) / 100);
    const total = subtotalWithMarkup + taxAmount;
    
    return {
      materialCost,
      laborCost,
      equipmentCost,
      subtotal,
      markupAmount,
      taxAmount,
      total,
      laborHours: (item.laborHours || 0) * baseQuantity
    };
  };
  
  // Calculate total estimate cost
  const calculateEstimateTotal = () => {
    return sections.reduce((sectionTotal, section) => {
      return sectionTotal + section.items.reduce((itemTotal, item) => {
        return itemTotal + (item.total || 0);
      }, 0);
    }, 0);
  };
  
  // Render loading state
  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#0066cc" />
        <Text style={styles.loadingText}>Loading estimate data...</Text>
      </View>
    );
  }
  
  // Main render
  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={onCancel} style={styles.cancelButton}>
          <Text style={styles.cancelButtonText}>Cancel</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Estimate Editor</Text>
        <TouchableOpacity onPress={handleSaveEstimate} style={styles.saveHeaderButton}>
          <Text style={styles.saveHeaderButtonText}>Save</Text>
        </TouchableOpacity>
      </View>
      
      {/* Estimate Info */}
      {estimate && (
        <View style={styles.estimateInfo}>
          <Text style={styles.estimateTitle}>{estimate.title}</Text>
          <View style={styles.clientSelector}>
            <Text style={styles.clientLabel}>Client:</Text>
            <Text style={styles.clientName}>
              {selectedClient ? selectedClient.name : 'Select Client'}
            </Text>
          </View>
          <Text style={styles.estimateTotal}>
            Total: ${calculateEstimateTotal().toFixed(2)}
          </Text>
        </View>
      )}
      
      <ScrollView style={styles.content}>
        {/* Sections */}
        {sections.map((section, sectionIndex) => (
          <View key={section.id} style={styles.section}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>{section.name}</Text>
              <TouchableOpacity
                onPress={() => handleAddItem(sectionIndex)}
                style={styles.addItemButton}
              >
                <Text style={styles.addItemButtonText}>+ Add Item</Text>
              </TouchableOpacity>
            </View>
            
            {/* Items */}
            {section.items.map((item, itemIndex) => (
              <View key={item.id || itemIndex} style={styles.item}>
                <View style={styles.itemHeader}>
                  <Text style={styles.itemName}>{item.name || 'Unnamed Item'}</Text>
                  <TouchableOpacity
                    onPress={() => handleRemoveItem(sectionIndex, itemIndex)}
                    style={styles.removeButton}
                  >
                    <Text style={styles.removeButtonText}>×</Text>
                  </TouchableOpacity>
                </View>
                <Text style={styles.itemDescription}>{item.description || ''}</Text>
                <View style={styles.itemDetails}>
                  <View style={styles.quantityContainer}>
                    <Text style={styles.label}>Qty:</Text>
                    <TextInput
                      style={styles.quantityInput}
                      value={String(item.quantity || 1)}
                      onChangeText={(text) => handleUpdateQuantity(sectionIndex, itemIndex, parseFloat(text) || 0)}
                      keyboardType="numeric"
                    />
                  </View>
                  <Text style={styles.itemTotal}>${(item.total || 0).toFixed(2)}</Text>
                </View>
              </View>
            ))}
          </View>
        ))}
        
        {/* Add Section Button */}
        <TouchableOpacity onPress={handleAddSection} style={styles.addSectionButton}>
          <Text style={styles.addSectionButtonText}>+ Add Section</Text>
        </TouchableOpacity>
      </ScrollView>
      
      {/* Assembly Selector Modal */}
      <Modal
        visible={showAssemblySelector}
        animationType="slide"
        onRequestClose={() => setShowAssemblySelector(false)}
      >
        <View style={styles.modalContainer}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Select Assembly</Text>
            <TouchableOpacity onPress={() => setShowAssemblySelector(false)}>
              <Text style={styles.closeButton}>×</Text>
            </TouchableOpacity>
          </View>
          
          <TextInput
            style={styles.searchInput}
            value={searchQuery}
            onChangeText={handleSearchAssemblies}
            placeholder="Search assemblies..."
            autoFocus
          />
          
          <FlatList
            data={searchResults}
            keyExtractor={(item) => item.id}
            renderItem={({ item }) => (
              <TouchableOpacity
                onPress={() => handleSelectAssembly(item)}
                style={styles.assemblyItem}
              >
                <Text style={styles.assemblyName}>{item.name}</Text>
                <Text style={styles.assemblyDescription}>{item.description}</Text>
                <Text style={styles.assemblyCost}>
                  ${(item.estimatedCost || 0).toFixed(2)}
                </Text>
              </TouchableOpacity>
            )}
            ListEmptyComponent={
              <Text style={styles.emptyText}>
                {searchQuery.length < 3 
                  ? 'Type at least 3 characters to search' 
                  : 'No assemblies found'}
              </Text>
            }
          />
        </View>
      </Modal>
    </View>
  );
};

// API Functions - These would be implemented to connect to your backend
const fetchClients = async () => {
  // Mock implementation - replace with actual API call
  return [
    { id: '1', name: 'John Doe Construction' },
    { id: '2', name: 'ABC Builders' },
    { id: '3', name: 'XYZ Contractors' }
  ];
};

const fetchEstimate = async (estimateId) => {
  // Mock implementation - replace with actual API call
  return {
    id: estimateId,
    title: 'Sample Estimate',
    status: 'draft',
    clientId: '1',
    projectId: '',
    issueDate: new Date(),
    expiryDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
    taxRate: 8.25,
    markup: 20,
    terms: '',
    notes: '',
    sections: []
  };
};

const fetchTemplate = async (templateId) => {
  // Mock implementation - replace with actual API call
  return {
    id: templateId,
    name: 'Standard Template',
    defaultTaxRate: 8.25,
    defaultMarkup: 20,
    termsAndConditions: '',
    notes: '',
    customFields: {},
    sections: [
      {
        id: 'section-1',
        name: 'General',
        sequence: 1,
        items: []
      }
    ]
  };
};

const searchAssemblies = async (query) => {
  try {
    const response = await fetch(`/api/assemblies/search?q=${encodeURIComponent(query)}`);
    if (!response.ok) throw new Error('Failed to search assemblies');
    
    const assemblies = await response.json();
    
    // Transform catalogue data to match expected format
    return assemblies.map(assembly => ({
      id: assembly.id,
      name: assembly.name,
      description: assembly.description,
      estimatedCost: assembly.totalCost
    }));
  } catch (error) {
    console.error('Error searching assemblies:', error);
    
    // Fallback to sample data that matches catalogue entries
    const fallbackAssemblies = [
      {
        id: 'framing-assembly-1',
        name: 'Framing Assembly',
        description: 'Standard wall framing with studs',
        estimatedCost: 150.00
      },
      {
        id: 'electrical-rough-in-1',
        name: 'Electrical Rough-in',
        description: 'Basic electrical rough-in work',
        estimatedCost: 200.00
      },
      {
        id: 'plumbing-rough-in-1',
        name: 'Plumbing Rough-in',
        description: 'Standard plumbing rough-in',
        estimatedCost: 300.00
      }
    ];
    
    return fallbackAssemblies.filter(assembly => 
      assembly.name.toLowerCase().includes(query.toLowerCase()) ||
      assembly.description.toLowerCase().includes(query.toLowerCase())
    );
  }
};

const convertAssemblyToLineItem = async (assembly, quantity) => {
  // Mock implementation - replace with actual API call
  return {
    id: `item-${Date.now()}`,
    name: assembly.name,
    description: assembly.description,
    quantity: quantity,
    materialCostPerUnit: assembly.estimatedCost * 0.6,
    laborHours: 2,
    laborRate: 50,
    equipmentCostPerUnit: assembly.estimatedCost * 0.1,
    markupPercentage: 20,
    taxRate: 8.25,
    total: assembly.estimatedCost * quantity
  };
};

const createEstimate = async (estimateData) => {
  // Mock implementation - replace with actual API call
  const newEstimate = {
    ...estimateData,
    id: `estimate-${Date.now()}`,
    createdAt: new Date(),
    updatedAt: new Date()
  };
  console.log('Creating estimate:', newEstimate);
  return newEstimate;
};

const updateEstimate = async (estimateId, estimateData) => {
  // Mock implementation - replace with actual API call
  const updatedEstimate = {
    ...estimateData,
    id: estimateId,
    updatedAt: new Date()
  };
  console.log('Updating estimate:', updatedEstimate);
  return updatedEstimate;
};

// Styles
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#fff',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  cancelButton: {
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  cancelButtonText: {
    color: '#666',
    fontSize: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  saveHeaderButton: {
    backgroundColor: '#0066cc',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 4,
  },
  saveHeaderButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '500',
  },
  estimateInfo: {
    backgroundColor: '#fff',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  estimateTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  clientSelector: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  clientLabel: {
    fontSize: 16,
    color: '#666',
    marginRight: 8,
  },
  clientName: {
    fontSize: 16,
    color: '#0066cc',
    fontWeight: '500',
  },
  estimateTotal: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#0066cc',
    textAlign: 'right',
  },
  content: {
    flex: 1,
    padding: 16,
  },
  section: {
    backgroundColor: '#fff',
    borderRadius: 8,
    marginBottom: 16,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  addItemButton: {
    backgroundColor: '#0066cc',
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 4,
  },
  addItemButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '500',
  },
  item: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  itemName: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
    flex: 1,
  },
  removeButton: {
    backgroundColor: '#ff4444',
    width: 24,
    height: 24,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  removeButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  itemDescription: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  itemDetails: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  quantityContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    fontSize: 14,
    color: '#666',
    marginRight: 8,
  },
  quantityInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 4,
    padding: 8,
    width: 60,
    textAlign: 'center',
    fontSize: 14,
  },
  itemTotal: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#0066cc',
  },
  addSectionButton: {
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 16,
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#0066cc',
    borderStyle: 'dashed',
  },
  addSectionButtonText: {
    color: '#0066cc',
    fontSize: 16,
    fontWeight: '500',
  },
  modalContainer: {
    flex: 1,
    backgroundColor: '#fff',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
    backgroundColor: '#fff',
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  closeButton: {
    fontSize: 24,
    color: '#666',
    fontWeight: 'bold',
  },
  searchInput: {
    margin: 16,
    padding: 12,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    fontSize: 16,
    backgroundColor: '#fff',
  },
  assemblyItem: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
    backgroundColor: '#fff',
  },
  assemblyName: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
    marginBottom: 4,
  },
  assemblyDescription: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  assemblyCost: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#0066cc',
  },
  emptyText: {
    textAlign: 'center',
    fontSize: 16,
    color: '#666',
    marginTop: 32,
    paddingHorizontal: 16,
  },
});

export default EstimateEditor;