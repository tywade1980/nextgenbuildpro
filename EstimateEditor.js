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

/**
 * React Native EstimateEditor Component
 * 
 * This component provides the same functionality as the existing Kotlin
 * EnhancedEstimateEditorScreen.kt but in React Native format.
 * 
 * Based on the existing implementation in:
 * - app/src/main/java/com/nextgenbuildpro/features/estimates/EnhancedEstimateEditorScreen.kt
 * - app/src/main/java/com/nextgenbuildpro/features/estimates/AssemblySearchAndSelectionScreen.kt
 * - app/src/main/java/com/nextgenbuildpro/pm/data/repository/TemplateEstimateRepository.kt
 * - app/src/main/java/com/nextgenbuildpro/pm/service/AssemblyCatalogueService.kt
 */
const EstimateEditor = ({
  estimateId,
  templateId,
  clientId,
  projectId,
  onSave,
  onCancel
}) => {
  // State management - equivalent to Kotlin mutableStateOf
  const [estimate, setEstimate] = useState(null);
  const [sections, setSections] = useState([]);
  const [clients, setClients] = useState([]);
  const [selectedClient, setSelectedClient] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showAssemblySelector, setShowAssemblySelector] = useState(false);
  const [currentSectionIndex, setCurrentSectionIndex] = useState(-1);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [showTaxMarkupDialog, setShowTaxMarkupDialog] = useState(false);
  const [taxRate, setTaxRate] = useState('8.25');
  const [markupPercentage, setMarkupPercentage] = useState('20.0');
  const [errorMessage, setErrorMessage] = useState('');
  const [showError, setShowError] = useState(false);
  
  // Load estimate data on component mount - equivalent to LaunchedEffect
  useEffect(() => {
    const loadData = async () => {
      setIsLoading(true);
      
      try {
        // Load clients
        const clientsData = await fetchClients();
        setClients(clientsData);
        
        if (estimateId) {
          // Load existing estimate - equivalent to templateEstimateRepository.getById()
          const estimateData = await fetchEstimate(estimateId);
          setEstimate(estimateData);
          setSections(estimateData.sections || []);
          
          if (estimateData.clientId) {
            const client = clientsData.find((c) => c.id === estimateData.clientId);
            setSelectedClient(client || null);
          }
        } else if (templateId) {
          // Create new estimate from template - equivalent to template system
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
        setErrorMessage('Error loading estimate data: ' + error.message);
        setShowError(true);
      } finally {
        setIsLoading(false);
      }
    };
    
    loadData();
  }, [estimateId, templateId, clientId, projectId]);
  
  // Handle adding a new section - equivalent to assembly management
  const handleAddSection = () => {
    const newSection = {
      id: `section-${Date.now()}`,
      name: 'New Section',
      sequence: sections.length + 1,
      items: []
    };
    
    setSections([...sections, newSection]);
  };
  
  // Handle adding an item to a section - equivalent to showAssemblySearch = true
  const handleAddItem = (sectionIndex) => {
    setCurrentSectionIndex(sectionIndex);
    setShowAssemblySelector(true);
  };
  
  // Handle searching for assemblies - equivalent to AssemblyCatalogueService.searchAssemblies()
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
  
  // Handle selecting an assembly to add - equivalent to addAssemblyToEstimate()
  const handleSelectAssembly = async (assembly) => {
    if (currentSectionIndex < 0 || currentSectionIndex >= sections.length) {
      return;
    }
    
    try {
      // Equivalent to AssemblyCatalogueService.convertTemplateToAssembly()
      const lineItem = await convertAssemblyToLineItem(assembly, 1);
      
      const updatedSections = [...sections];
      updatedSections[currentSectionIndex].items.push(lineItem);
      
      setSections(updatedSections);
      setShowAssemblySelector(false);
      setSearchQuery('');
      setSearchResults([]);
    } catch (error) {
      console.error('Error adding assembly:', error);
      setErrorMessage('Error adding assembly: ' + error.message);
      setShowError(true);
    }
  };
  
  // Handle removing an item - equivalent to removeAssemblyFromEstimate()
  const handleRemoveItem = (sectionIndex, itemIndex) => {
    const updatedSections = [...sections];
    updatedSections[sectionIndex].items.splice(itemIndex, 1);
    
    setSections(updatedSections);
  };
  
  // Handle updating item quantity - equivalent to calculation engine updates
  const handleUpdateQuantity = (sectionIndex, itemIndex, quantity) => {
    const updatedSections = [...sections];
    updatedSections[sectionIndex].items[itemIndex].quantity = quantity;
    
    // Recalculate totals - equivalent to CalculationEngineService
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
  
  // Apply tax and markup - equivalent to applyTaxAndMarkup()
  const handleApplyTaxAndMarkup = async () => {
    if (!estimate) return;
    
    try {
      const taxSettings = {
        type: 'PERCENTAGE',
        rate: parseFloat(taxRate) || 0.0
      };
      
      const markupSettings = {
        type: 'PERCENTAGE',
        value: parseFloat(markupPercentage) || 0.0
      };
      
      const success = await applyTaxAndMarkup(estimate.id, taxSettings, markupSettings);
      
      if (success) {
        const updatedEstimate = await fetchEstimate(estimate.id);
        setEstimate(updatedEstimate);
        setShowTaxMarkupDialog(false);
      } else {
        setErrorMessage('Failed to apply tax and markup');
        setShowError(true);
      }
    } catch (error) {
      setErrorMessage('Error applying tax and markup: ' + error.message);
      setShowError(true);
    }
  };
  
  // Handle saving the estimate - equivalent to TemplateEstimateRepository operations
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
        // Create new estimate - equivalent to templateEstimateRepository.create()
        const newEstimate = await createEstimate(estimateData);
        savedEstimateId = newEstimate.id;
      } else {
        // Update existing estimate - equivalent to templateEstimateRepository.update()
        await updateEstimate(estimate.id, estimateData);
        savedEstimateId = estimate.id;
      }
      
      // Call the onSave callback if provided
      if (onSave) {
        onSave(savedEstimateId, estimateData);
      }
      
    } catch (error) {
      console.error('Error saving estimate:', error);
      setErrorMessage('Error saving estimate: ' + error.message);
      setShowError(true);
    } finally {
      setIsLoading(false);
    }
  };
  
  // Calculate line item totals - equivalent to CalculationEngineService calculations
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
  
  // Render loading state
  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#0066cc" />
        <Text style={styles.loadingText}>Loading estimate data...</Text>
      </View>
    );
  }
  
  // Render main UI
  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={onCancel} style={styles.cancelButton}>
          <Text style={styles.cancelButtonText}>Cancel</Text>
        </TouchableOpacity>
        <Text style={styles.title}>Estimate Editor</Text>
        <TouchableOpacity onPress={() => setShowTaxMarkupDialog(true)} style={styles.settingsButton}>
          <Text style={styles.settingsButtonText}>⚙️</Text>
        </TouchableOpacity>
      </View>
      
      {/* Estimate Summary */}
      {estimate && (
        <View style={styles.summaryCard}>
          <Text style={styles.summaryTitle}>{estimate.title}</Text>
          <Text style={styles.summaryClient}>Client: {selectedClient?.name || 'No client selected'}</Text>
          <Text style={styles.summaryStatus}>Status: {estimate.status}</Text>
        </View>
      )}
      
      {/* Sections List */}
      <ScrollView style={styles.sectionsContainer}>
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
            
            {/* Section Items */}
            {section.items.map((item, itemIndex) => (
              <View key={item.id} style={styles.item}>
                <Text style={styles.itemName}>{item.name}</Text>
                <Text style={styles.itemDescription}>{item.description}</Text>
                <View style={styles.itemDetails}>
                  <TextInput
                    style={styles.quantityInput}
                    value={item.quantity?.toString() || '1'}
                    onChangeText={(text) => handleUpdateQuantity(sectionIndex, itemIndex, parseFloat(text) || 0)}
                    placeholder="Qty"
                    keyboardType="numeric"
                  />
                  <Text style={styles.itemCost}>${item.total?.toFixed(2) || '0.00'}</Text>
                  <TouchableOpacity
                    onPress={() => handleRemoveItem(sectionIndex, itemIndex)}
                    style={styles.removeButton}
                  >
                    <Text style={styles.removeButtonText}>Remove</Text>
                  </TouchableOpacity>
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
      
      {/* Save Button */}
      <TouchableOpacity onPress={handleSaveEstimate} style={styles.saveButton}>
        <Text style={styles.saveButtonText}>Save Estimate</Text>
      </TouchableOpacity>
      
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
              <Text style={styles.modalCloseButton}>✕</Text>
            </TouchableOpacity>
          </View>
          
          <TextInput
            style={styles.searchInput}
            value={searchQuery}
            onChangeText={handleSearchAssemblies}
            placeholder="Search assemblies..."
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
                <Text style={styles.assemblyCost}>${item.estimatedCost?.toFixed(2) || '0.00'}</Text>
              </TouchableOpacity>
            )}
          />
        </View>
      </Modal>
      
      {/* Tax & Markup Dialog */}
      <Modal
        visible={showTaxMarkupDialog}
        transparent={true}
        animationType="fade"
        onRequestClose={() => setShowTaxMarkupDialog(false)}
      >
        <View style={styles.overlayContainer}>
          <View style={styles.dialogContainer}>
            <Text style={styles.dialogTitle}>Tax & Markup Settings</Text>
            
            <TextInput
              style={styles.dialogInput}
              value={taxRate}
              onChangeText={setTaxRate}
              placeholder="Tax Rate (%)"
              keyboardType="numeric"
            />
            
            <TextInput
              style={styles.dialogInput}
              value={markupPercentage}
              onChangeText={setMarkupPercentage}
              placeholder="Markup (%)"
              keyboardType="numeric"
            />
            
            <View style={styles.dialogButtons}>
              <TouchableOpacity
                onPress={() => setShowTaxMarkupDialog(false)}
                style={styles.dialogCancelButton}
              >
                <Text style={styles.dialogCancelButtonText}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity
                onPress={handleApplyTaxAndMarkup}
                style={styles.dialogApplyButton}
              >
                <Text style={styles.dialogApplyButtonText}>Apply</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
      
      {/* Error Dialog */}
      <Modal
        visible={showError}
        transparent={true}
        animationType="fade"
        onRequestClose={() => setShowError(false)}
      >
        <View style={styles.overlayContainer}>
          <View style={styles.dialogContainer}>
            <Text style={styles.dialogTitle}>Error</Text>
            <Text style={styles.errorMessage}>{errorMessage}</Text>
            <TouchableOpacity
              onPress={() => setShowError(false)}
              style={styles.dialogApplyButton}
            >
              <Text style={styles.dialogApplyButtonText}>OK</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </View>
  );
};

// API Functions (These would connect to your backend/services)
// These are equivalents to the Kotlin repository and service methods

const fetchClients = async () => {
  // Equivalent to client repository operations
  // Implementation would call your backend API
  return [];
};

const fetchEstimate = async (estimateId) => {
  // Equivalent to TemplateEstimateRepository.getById()
  // Implementation would call your backend API
  return null;
};

const fetchTemplate = async (templateId) => {
  // Equivalent to template system operations
  // Implementation would call your backend API
  return null;
};

const searchAssemblies = async (query) => {
  // Equivalent to AssemblyCatalogueService.searchAssemblies()
  // Implementation would call your backend API
  return [];
};

const convertAssemblyToLineItem = async (assembly, quantity) => {
  // Equivalent to AssemblyCatalogueService.convertTemplateToAssembly()
  // Implementation would call your backend API
  return null;
};

const createEstimate = async (estimateData) => {
  // Equivalent to TemplateEstimateRepository.create()
  // Implementation would call your backend API
  return null;
};

const updateEstimate = async (estimateId, estimateData) => {
  // Equivalent to TemplateEstimateRepository.update()
  // Implementation would call your backend API
  return null;
};

const applyTaxAndMarkup = async (estimateId, taxSettings, markupSettings) => {
  // Equivalent to TemplateEstimateRepository.applyTaxAndMarkup()
  // Implementation would call your backend API
  return false;
};

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
    padding: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  cancelButton: {
    padding: 8,
  },
  cancelButtonText: {
    color: '#0066cc',
    fontSize: 16,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  settingsButton: {
    padding: 8,
  },
  settingsButtonText: {
    fontSize: 18,
  },
  summaryCard: {
    backgroundColor: '#fff',
    margin: 16,
    padding: 16,
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  summaryTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  summaryClient: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  summaryStatus: {
    fontSize: 14,
    color: '#666',
  },
  sectionsContainer: {
    flex: 1,
    paddingHorizontal: 16,
  },
  section: {
    backgroundColor: '#fff',
    marginBottom: 16,
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
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
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  addItemButton: {
    backgroundColor: '#0066cc',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 4,
  },
  addItemButtonText: {
    color: '#fff',
    fontSize: 14,
  },
  item: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  itemName: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
    marginBottom: 4,
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
  quantityInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 4,
    padding: 8,
    width: 80,
    textAlign: 'center',
  },
  itemCost: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#0066cc',
  },
  removeButton: {
    backgroundColor: '#ff4444',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 4,
  },
  removeButtonText: {
    color: '#fff',
    fontSize: 12,
  },
  addSectionButton: {
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  addSectionButtonText: {
    color: '#0066cc',
    fontSize: 16,
    fontWeight: '500',
  },
  saveButton: {
    backgroundColor: '#0066cc',
    margin: 16,
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
  },
  saveButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
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
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  modalCloseButton: {
    fontSize: 24,
    color: '#666',
  },
  searchInput: {
    margin: 16,
    padding: 12,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 8,
    fontSize: 16,
  },
  assemblyItem: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
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
    marginBottom: 4,
  },
  assemblyCost: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#0066cc',
  },
  overlayContainer: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  dialogContainer: {
    backgroundColor: '#fff',
    margin: 32,
    padding: 24,
    borderRadius: 8,
    minWidth: 280,
  },
  dialogTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
    textAlign: 'center',
  },
  dialogInput: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 4,
    padding: 12,
    marginBottom: 16,
    fontSize: 16,
  },
  dialogButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  dialogCancelButton: {
    flex: 1,
    padding: 12,
    marginRight: 8,
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 4,
    alignItems: 'center',
  },
  dialogCancelButtonText: {
    color: '#666',
    fontSize: 16,
  },
  dialogApplyButton: {
    flex: 1,
    padding: 12,
    marginLeft: 8,
    backgroundColor: '#0066cc',
    borderRadius: 4,
    alignItems: 'center',
  },
  dialogApplyButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  errorMessage: {
    fontSize: 14,
    color: '#666',
    marginBottom: 16,
    textAlign: 'center',
  },
});

export default EstimateEditor;