import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import EstimateEditor from './EstimateEditorComplete';

/**
 * Example usage of the completed EstimateEditor component
 * This demonstrates how to use the component in different scenarios
 */
const EstimateEditorExample = () => {
  const [currentView, setCurrentView] = useState('menu');
  const [estimateId, setEstimateId] = useState(null);

  const handleSaveEstimate = (savedEstimateId, estimateData) => {
    console.log('Estimate saved with ID:', savedEstimateId);
    console.log('Estimate data:', estimateData);
    Alert.alert(
      'Success',
      `Estimate saved successfully!\nID: ${savedEstimateId}`,
      [{ text: 'OK', onPress: () => setCurrentView('menu') }]
    );
  };

  const handleCancel = () => {
    setCurrentView('menu');
  };

  const renderMenu = () => (
    <View style={styles.menuContainer}>
      <Text style={styles.title}>EstimateEditor Examples</Text>
      <Text style={styles.subtitle}>Choose a scenario to test:</Text>
      
      <TouchableOpacity 
        style={styles.menuButton}
        onPress={() => setCurrentView('new')}
      >
        <Text style={styles.menuButtonText}>Create New Blank Estimate</Text>
        <Text style={styles.menuButtonSubtext}>Start with empty estimate</Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={styles.menuButton}
        onPress={() => setCurrentView('template')}
      >
        <Text style={styles.menuButtonText}>Create from Template</Text>
        <Text style={styles.menuButtonSubtext}>Use predefined template</Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={styles.menuButton}
        onPress={() => setCurrentView('edit')}
      >
        <Text style={styles.menuButtonText}>Edit Existing Estimate</Text>
        <Text style={styles.menuButtonSubtext}>Load and modify existing</Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={styles.menuButton}
        onPress={() => setCurrentView('client')}
      >
        <Text style={styles.menuButtonText}>New Estimate for Client</Text>
        <Text style={styles.menuButtonSubtext}>Start with client pre-selected</Text>
      </TouchableOpacity>
    </View>
  );

  const renderEstimateEditor = () => {
    let props = {
      onSave: handleSaveEstimate,
      onCancel: handleCancel
    };

    switch (currentView) {
      case 'new':
        // Create new blank estimate
        break;
      case 'template':
        // Create from template
        props.templateId = 'template-123';
        break;
      case 'edit':
        // Edit existing estimate
        props.estimateId = 'estimate-456';
        break;
      case 'client':
        // New estimate with client
        props.clientId = '1';
        props.projectId = 'project-789';
        break;
      default:
        return renderMenu();
    }

    return <EstimateEditor {...props} />;
  };

  return (
    <View style={styles.container}>
      {currentView === 'menu' ? renderMenu() : renderEstimateEditor()}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  menuContainer: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 32,
  },
  menuButton: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 8,
    marginBottom: 16,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  menuButtonText: {
    fontSize: 18,
    fontWeight: '500',
    color: '#333',
    marginBottom: 4,
  },
  menuButtonSubtext: {
    fontSize: 14,
    color: '#666',
  },
});

export default EstimateEditorExample;

// Example of how to integrate into your main App.js:
/*
import React from 'react';
import EstimateEditorExample from './EstimateEditorExample';

export default function App() {
  return <EstimateEditorExample />;
}
*/