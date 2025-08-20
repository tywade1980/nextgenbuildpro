package com.nextgenbuildpro.features.estimates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nextgenbuildpro.pm.data.model.EstimateItem
import com.nextgenbuildpro.pm.rememberPmComponents
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Screen for creating or editing estimate items
 * @param navController Navigation controller
 * @param estimateId ID of the estimate this item belongs to
 * @param itemId Optional ID of the item to edit. If null, a new item will be created.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimateItemEditorScreen(
    navController: NavController,
    estimateId: String,
    itemId: String? = null
) {
    val pmComponents = rememberPmComponents()
    val estimateRepository = pmComponents.estimateRepository
    val coroutineScope = rememberCoroutineScope()
    
    // State for form fields
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1.0") }
    var unitPrice by remember { mutableStateOf("0.0") }
    var unit by remember { mutableStateOf("each") }
    var type by remember { mutableStateOf("Material") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    
    // State for validation
    var nameError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var unitPriceError by remember { mutableStateOf<String?>(null) }
    
    // State for saving
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // Load existing item data if editing
    LaunchedEffect(itemId) {
        if (itemId != null && itemId != "null") {
            val estimate = estimateRepository.getById(estimateId)
            estimate?.items?.find { it.id == itemId }?.let { item ->
                name = item.name
                description = item.description
                quantity = item.quantity.toString()
                unitPrice = item.unitPrice.toString()
                unit = item.unit
                type = item.type
                categoryId = item.categoryId
            }
        }
    }
    
    // Available item types
    val itemTypes = listOf("Material", "Labor", "Equipment", "Subcontractor", "Other")
    
    // Common units
    val commonUnits = listOf("each", "sq ft", "sq yd", "linear ft", "hour", "day", "week", "month")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId != null && itemId != "null") "Edit Item" else "Add Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Validate form
                            var isValid = true
                            
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                isValid = false
                            } else {
                                nameError = null
                            }
                            
                            try {
                                val qtyValue = quantity.toDouble()
                                if (qtyValue <= 0) {
                                    quantityError = "Quantity must be greater than 0"
                                    isValid = false
                                } else {
                                    quantityError = null
                                }
                            } catch (e: NumberFormatException) {
                                quantityError = "Invalid number format"
                                isValid = false
                            }
                            
                            try {
                                val priceValue = unitPrice.toDouble()
                                if (priceValue < 0) {
                                    unitPriceError = "Price cannot be negative"
                                    isValid = false
                                } else {
                                    unitPriceError = null
                                }
                            } catch (e: NumberFormatException) {
                                unitPriceError = "Invalid number format"
                                isValid = false
                            }
                            
                            if (isValid) {
                                // Save item
                                coroutineScope.launch {
                                    isSaving = true
                                    
                                    val item = EstimateItem(
                                        id = itemId?.takeIf { it != "null" } ?: UUID.randomUUID().toString(),
                                        name = name,
                                        description = description,
                                        quantity = quantity.toDouble(),
                                        unitPrice = unitPrice.toDouble(),
                                        unit = unit,
                                        type = type,
                                        categoryId = categoryId
                                    )
                                    
                                    val success = if (itemId != null && itemId != "null") {
                                        estimateRepository.updateEstimateItem(estimateId, item)
                                    } else {
                                        estimateRepository.addItemToEstimate(estimateId, item)
                                    }
                                    
                                    isSaving = false
                                    
                                    if (success) {
                                        showSuccessMessage = true
                                        // Navigate back after a short delay
                                        kotlinx.coroutines.delay(1500)
                                        navController.navigateUp()
                                    }
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } }
                )
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    minLines = 3
                )
                
                // Type dropdown
                Text(
                    text = "Item Type",
                    style = MaterialTheme.typography.bodyMedium
                )
                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        itemTypes.forEach { itemType ->
                            DropdownMenuItem(
                                text = { Text(itemType) },
                                onClick = {
                                    type = itemType
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Quantity and Unit Price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quantity field
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = quantityError != null,
                        supportingText = { quantityError?.let { Text(it) } }
                    )
                    
                    // Unit Price field
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Unit Price*") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = unitPriceError != null,
                        supportingText = { unitPriceError?.let { Text(it) } }
                    )
                }
                
                // Unit dropdown
                Text(
                    text = "Unit",
                    style = MaterialTheme.typography.bodyMedium
                )
                var unitExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it }
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        commonUnits.forEach { unitOption ->
                            DropdownMenuItem(
                                text = { Text(unitOption) },
                                onClick = {
                                    unit = unitOption
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Total calculation
                val total = try {
                    quantity.toDouble() * unitPrice.toDouble()
                } catch (e: NumberFormatException) {
                    0.0
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Item Total",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "$${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                
                // Spacer at the bottom for better scrolling
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Success message
            if (showSuccessMessage) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = if (itemId != null && itemId != "null") "Item updated successfully" else "Item added successfully",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Loading indicator
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}