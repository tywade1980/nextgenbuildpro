# Quick Start: Building Estimates for Clients

## 🎯 Overview
This guide shows you how to build construction estimates for clients using the newly implemented EstimateAPIService.

---

## 📱 For Mobile Developers (Kotlin/Android)

### Step 1: Initialize the Service

```kotlin
import com.nextgenbuildpro.pm.service.EstimateAPIService

// In your Activity or ViewModel
val estimateAPI = EstimateAPIService.getInstance(context)
```

### Step 2: Fetch Clients

```kotlin
lifecycleScope.launch {
    val result = estimateAPI.fetchClients()
    
    result.onSuccess { clients ->
        // Display clients in a list
        clients.forEach { client ->
            println("${client.name} - ${client.email}")
        }
    }.onFailure { error ->
        Log.e("EstimateApp", "Failed to fetch clients", error)
    }
}
```

### Step 3: Search Construction Assemblies

```kotlin
lifecycleScope.launch {
    // Search for foundation work
    val result = estimateAPI.searchAssemblies("foundation")
    
    result.onSuccess { assemblies ->
        assemblies.forEach { assembly ->
            val name = assembly.getString("name")
            val cost = assembly.getDouble("estimatedCost")
            println("$name - $$cost")
        }
    }
}
```

### Step 4: Convert Assembly to Line Item

```kotlin
lifecycleScope.launch {
    val result = estimateAPI.convertAssemblyToLineItem(
        assemblyId = "assembly-123",
        quantity = 2.5
    )
    
    result.onSuccess { lineItem ->
        val name = lineItem.getString("name")
        val totalCost = lineItem.getDouble("totalCost")
        println("Added: $name - $$totalCost")
    }
}
```

### Step 5: Create New Estimate

```kotlin
import org.json.JSONObject

lifecycleScope.launch {
    val estimateData = JSONObject().apply {
        put("id", "estimate-${System.currentTimeMillis()}")
        put("projectId", "project-001")
        put("title", "Custom Home Foundation")
    }
    
    val result = estimateAPI.createEstimate(estimateData)
    
    result.onSuccess { estimate ->
        println("Estimate created: ${estimate.id}")
        // Navigate to estimate detail view
    }
}
```

### Step 6: Apply Tax and Markup

```kotlin
import com.nextgenbuildpro.pm.service.TaxSettings
import com.nextgenbuildpro.pm.service.MarkupSettings
import com.nextgenbuildpro.pm.service.TaxType
import com.nextgenbuildpro.pm.service.MarkupType

lifecycleScope.launch {
    val taxSettings = TaxSettings(
        taxType = TaxType.PERCENTAGE,
        rate = 8.5
    )
    
    val markupSettings = MarkupSettings(
        markupType = MarkupType.PERCENTAGE,
        rate = 15.0
    )
    
    val result = estimateAPI.applyTaxAndMarkup(
        estimateId = "estimate-123",
        taxSettings = taxSettings,
        markupSettings = markupSettings
    )
    
    result.onSuccess { updatedEstimate ->
        println("Grand Total: ${updatedEstimate.grandTotal}")
    }
}
```

---

## 🌐 For Web Developers (React Native/JavaScript)

### Step 1: Import Component

```javascript
import EstimateEditor from './EstimateEditor';
```

### Step 2: Use in Your App

```javascript
function MyApp() {
  const [showEditor, setShowEditor] = useState(false);
  
  const handleSaveEstimate = (estimateId, estimateData) => {
    console.log('Estimate saved!', estimateId);
    // Save to backend, show success message, etc.
    setShowEditor(false);
  };
  
  return (
    <View>
      <Button 
        title="Create New Estimate" 
        onPress={() => setShowEditor(true)}
      />
      
      {showEditor && (
        <EstimateEditor
          estimateId="new"
          clientId="client-123"
          projectId="project-456"
          onSave={handleSaveEstimate}
          onCancel={() => setShowEditor(false)}
        />
      )}
    </View>
  );
}
```

### Step 3: Use with Existing Estimate

```javascript
<EstimateEditor
  estimateId="existing-estimate-789"
  onSave={(id, data) => console.log('Updated:', id)}
  onCancel={() => navigation.goBack()}
/>
```

### Step 4: Use with Template

```javascript
<EstimateEditor
  estimateId="new"
  templateId="template-residential-foundation"
  clientId="client-123"
  projectId="project-456"
  onSave={handleSave}
  onCancel={handleCancel}
/>
```

---

## 💡 Complete Example: Full Estimate Flow

```kotlin
class EstimateBuilderActivity : ComponentActivity() {
    private lateinit var estimateAPI: EstimateAPIService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        estimateAPI = EstimateAPIService.getInstance(this)
        
        lifecycleScope.launch {
            createCompleteEstimate()
        }
    }
    
    private suspend fun createCompleteEstimate() {
        // 1. Fetch clients
        val clientsResult = estimateAPI.fetchClients()
        val clients = clientsResult.getOrNull() ?: return
        val selectedClient = clients.first()
        
        println("Building estimate for: ${selectedClient.name}")
        
        // 2. Create new estimate
        val estimateData = JSONObject().apply {
            put("projectId", "custom-home-2024")
            put("title", "Custom Home Construction Estimate")
        }
        
        val estimateResult = estimateAPI.createEstimate(estimateData)
        val estimate = estimateResult.getOrNull() ?: return
        
        println("Estimate created: ${estimate.id}")
        
        // 3. Search and add assemblies
        val foundationResult = estimateAPI.searchAssemblies("foundation")
        val foundations = foundationResult.getOrNull() ?: return
        
        // 4. Convert first assembly to line item
        if (foundations.isNotEmpty()) {
            val firstFoundation = foundations.first()
            val assemblyId = firstFoundation.getString("id")
            
            val lineItemResult = estimateAPI.convertAssemblyToLineItem(
                assemblyId = assemblyId,
                quantity = 1.0
            )
            
            lineItemResult.onSuccess { lineItem ->
                println("Added assembly: ${lineItem.getString("name")}")
                println("Cost: $${lineItem.getDouble("totalCost")}")
            }
        }
        
        // 5. Apply tax and markup
        val finalResult = estimateAPI.applyTaxAndMarkup(
            estimateId = estimate.id,
            taxSettings = TaxSettings(TaxType.PERCENTAGE, 8.5),
            markupSettings = MarkupSettings(MarkupType.PERCENTAGE, 15.0)
        )
        
        finalResult.onSuccess { finalEstimate ->
            println("✅ Estimate complete!")
            println("Subtotal Labor: ${finalEstimate.subtotalLabor}")
            println("Subtotal Material: ${finalEstimate.subtotalMaterial}")
            println("Markup: ${finalEstimate.markupTotal}")
            println("Grand Total: ${finalEstimate.grandTotal}")
        }
    }
}
```

---

## 🔍 Available Assembly Categories

When searching assemblies, you can use these keywords:

- **Foundation**: "foundation", "footing", "concrete", "excavation"
- **Framing**: "framing", "studs", "wall", "structural"
- **Electrical**: "electrical", "wiring", "outlet", "panel"
- **Plumbing**: "plumbing", "pipe", "fixture", "drain"
- **Interior**: "drywall", "paint", "flooring", "trim"
- **Exterior**: "siding", "roofing", "windows", "doors"
- **HVAC**: "hvac", "heating", "cooling", "ventilation"

---

## 📊 Estimate Data Structure

### TemplateEstimate
```kotlin
data class TemplateEstimate(
    val id: String,
    val projectId: String,
    val contextMode: ContextMode,
    val assemblies: MutableList<TemplateAssembly>,
    val subtotalLabor: Double,
    val subtotalMaterial: Double,
    val markupTotal: Double,
    val grandTotal: Double,
    val createdAt: LocalDateTime,
    val status: EstimateStatus
)
```

### Assembly JSON Structure
```json
{
  "id": "assembly-123",
  "name": "Full Basement Foundation",
  "description": "Complete basement foundation with waterproofing",
  "estimatedCost": 30000.0,
  "unit": "each",
  "laborHours": 120.0,
  "materialCost": 18000.0,
  "laborCost": 6000.0
}
```

---

## 🚨 Error Handling

Always handle errors gracefully:

```kotlin
lifecycleScope.launch {
    val result = estimateAPI.createEstimate(estimateData)
    
    result.fold(
        onSuccess = { estimate ->
            // Success! Show the estimate
            showEstimateScreen(estimate)
        },
        onFailure = { error ->
            // Error occurred
            showErrorDialog("Failed to create estimate: ${error.message}")
            Log.e("EstimateApp", "Error creating estimate", error)
        }
    )
}
```

---

## 🎨 UI Integration Tips

### Display Client List
```kotlin
@Composable
fun ClientList() {
    val estimateAPI = EstimateAPIService.getInstance(LocalContext.current)
    var clients by remember { mutableStateOf<List<ClientInfo>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        estimateAPI.fetchClients().onSuccess { 
            clients = it 
        }
    }
    
    LazyColumn {
        items(clients) { client ->
            ClientCard(client) { selectedClient ->
                // Start estimate for this client
            }
        }
    }
}
```

### Display Assembly Search Results
```kotlin
@Composable
fun AssemblySearchResults(query: String) {
    val estimateAPI = EstimateAPIService.getInstance(LocalContext.current)
    var assemblies by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    
    LaunchedEffect(query) {
        estimateAPI.searchAssemblies(query).onSuccess {
            assemblies = it
        }
    }
    
    LazyColumn {
        items(assemblies.size) { index ->
            val assembly = assemblies[index]
            AssemblyCard(
                name = assembly.getString("name"),
                cost = assembly.getDouble("estimatedCost"),
                onAdd = { quantity ->
                    // Add to estimate
                }
            )
        }
    }
}
```

---

## ✅ Testing Your Implementation

### Unit Test Example
```kotlin
@Test
fun `test estimate creation flow`() = runTest {
    val estimateAPI = EstimateAPIService.getInstance(context)
    
    // Create estimate
    val data = JSONObject().apply {
        put("projectId", "test-project")
        put("title", "Test Estimate")
    }
    
    val result = estimateAPI.createEstimate(data)
    
    assertTrue(result.isSuccess)
    val estimate = result.getOrNull()
    assertNotNull(estimate)
    assertEquals("test-project", estimate?.projectId)
}
```

---

## 📞 Need Help?

- **Documentation**: See FINAL_IMPLEMENTATION_SUMMARY.md
- **Architecture**: See AGENT_ARCHITECTURE.md
- **Catalogue**: See CATALOGUE_SEEDING_README.md
- **Frontend**: See README_FRONTEND.md

---

## 🎉 You're Ready!

With EstimateAPIService and EstimateEditor, you can now:
- ✅ Fetch clients
- ✅ Search construction assemblies
- ✅ Build detailed estimates
- ✅ Apply tax and markup
- ✅ Save and retrieve estimates
- ✅ Generate professional quotes for clients

**Start building estimates today!** 🚀
