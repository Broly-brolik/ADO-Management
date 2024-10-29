package com.example.aguadeoromanagement.screens

import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aguadeoromanagement.components.Spinner
import com.example.aguadeoromanagement.models.Inventory
import com.example.aguadeoromanagement.models.OptionValue
import com.example.aguadeoromanagement.networking.api.getProduct
import kotlinx.coroutines.launch

@Composable
fun AddProductScreen(
    optionValues: List<OptionValue>,
    loading: Boolean,
    insertInventory: suspend (inventory: Inventory) -> Unit,
    getLastInventory: suspend (String) -> Inventory
) {
    var inventory by remember {
        mutableStateOf(Inventory())
    }
    val categoryList = optionValues.filter { optionValue -> optionValue.type == "ArticleType" }
        .map { it.optionValue }
    var categoryIndex by remember {
        mutableStateOf(-1)
    }

    val materialList = optionValues.filter { optionValue -> optionValue.type == "MaterialType" }
        .map { it.optionValue }
    var materialIndex by remember {
        mutableStateOf(-1)
    }

    var onWebsite by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    if (loading) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
        return
    }

    Column(Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                CustomTextField(
                    value = inventory.inventoryCode,
                    onChange = { inventory = inventory.copy(inventoryCode = it) },
                    "Inventory Code"
                )
                CustomTextField(
                    value = inventory.catalogCode,
                    onChange = { inventory = inventory.copy(catalogCode = it) },
                    "Catalog Code"
                )
                CustomTextField(
                    value = inventory.code,
                    onChange = { inventory = inventory.copy(code = it) },
                    "Code"
                )
                CustomTextField(
                    value = inventory.mark,
                    onChange = { inventory = inventory.copy(mark = it) },
                    "Mark"
                )
                Spinner(
                    entries = categoryList,
                    index = categoryIndex,
                    label = "Category",
                    onValueChange = { newIndex ->
                        categoryIndex = newIndex
                        inventory = inventory.copy(category = categoryList[newIndex])
                        scope.launch {
                            if (inventory.inventoryCode.isEmpty()) {
                                val lastInv = getLastInventory(categoryList[newIndex])
//                            inventory = inventory.copy(inventoryCode = lastInv.inventoryCode)
                                inventory = lastInv.copy()
                            }
                        }
                    }
                )
                CustomTextField(
                    value = inventory.description,
                    onChange = { inventory = inventory.copy(description = it) },
                    "Description"
                )
                CustomTextField(
                    value = inventory.width,
                    onChange = { inventory = inventory.copy(width = it) },
                    "Width",
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)

                )
                CustomTextField(
                    value = inventory.height,
                    onChange = { inventory = inventory.copy(height = it) },
                    "Height",
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)

                )
                Spinner(
                    entries = materialList,
                    index = materialIndex,
                    label = "Material",
                    onValueChange = { newIndex ->
                        materialIndex = newIndex
                        inventory = inventory.copy(material = materialList[newIndex])
                    }
                )
                CustomTextField(
                    value = inventory.color,
                    onChange = { inventory = inventory.copy(color = it) },
                    "Color",
                )
                CustomTextField(
                    value = inventory.weight,
                    onChange = { inventory = inventory.copy(weight = it) },
                    "Weight",
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                CustomTextField(
                    value = inventory.size,
                    onChange = { inventory = inventory.copy(size = it) },
                    "Size",
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                CustomTextField(
                    value = inventory.remark,
                    onChange = { inventory = inventory.copy(remark = it) },
                    "Remark",
                )

            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                CustomTextField(
                    value = inventory.stone,
                    onChange = { inventory = inventory.copy(stone = it) },
                    "Stone"
                )
                CustomTextField(
                    value = inventory.stoneQuantity,
                    onChange = { inventory = inventory.copy(stoneQuantity = it) },
                    "Stone quantity"
                )
                CustomTextField(
                    value = inventory.stoneType,
                    onChange = { inventory = inventory.copy(stoneType = it) },
                    "Stone type"
                )
                CustomTextField(
                    value = inventory.stoneSize,
                    onChange = { inventory = inventory.copy(stoneSize = it) },
                    "Stone size"
                )
                CustomTextField(
                    value = inventory.carat,
                    onChange = { inventory = inventory.copy(carat = it) },
                    "Carat",
                )
                CustomTextField(
                    value = inventory.stoneColor,
                    onChange = { inventory = inventory.copy(stoneColor = it) },
                    "Stone color"
                )
                CustomTextField(
                    value = inventory.stoneClarity,
                    onChange = { inventory = inventory.copy(stoneClarity = it) },
                    "Stone clarity"
                )
                CustomTextField(
                    value = inventory.stoneCut,
                    onChange = { inventory = inventory.copy(stoneCut = it) },
                    "Stone cut"
                )
                CustomTextField(
                    value = inventory.stonePolish,
                    onChange = { inventory = inventory.copy(stonePolish = it) },
                    "Stone polish"
                )
                CustomTextField(
                    value = inventory.stoneSymmetry,
                    onChange = { inventory = inventory.copy(stoneSymmetry = it) },
                    "Stone symmetry"
                )
                CustomTextField(
                    value = inventory.LAB,
                    onChange = { inventory = inventory.copy(LAB = it) },
                    "LAB"
                )
                CustomTextField(
                    value = inventory.certificate,
                    onChange = { inventory = inventory.copy(certificate = it) },
                    "Certificate"
                )

            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                CustomTextField(
                    value = inventory.status,
                    onChange = { inventory = inventory.copy(status = it) },
                    "Status"
                )
                CustomTextField(
                    value = inventory.shape,
                    onChange = { inventory = inventory.copy(shape = it) },
                    "Shape"
                )
                CustomTextField(
                    value = inventory.collection,
                    onChange = { inventory = inventory.copy(collection = it) },
                    "Collection"
                )
                CustomTextField(
                    value = inventory.line,
                    onChange = { inventory = inventory.copy(line = it) },
                    "Line"
                )
                CustomTextField(
                    value = inventory.style,
                    onChange = { inventory = inventory.copy(style = it) },
                    "Style"
                )
                CustomTextField(
                    value = inventory.setting,
                    onChange = { inventory = inventory.copy(setting = it) },
                    "Setting"
                )
                CustomTextField(
                    value = inventory.name,
                    onChange = { inventory = inventory.copy(name = it) },
                    "Name"
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CustomTextField(
                        value = inventory.ringPrize.toString(),
                        onChange = { inventory = inventory.copy(ringPrize = it) },
                        "Ring prize",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    CustomTextField(
                        value = inventory.prRing.toString(),
                        onChange = { inventory = inventory.copy(prRing = it) },
                        "prRing",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(
                        value = inventory.meleePrize.toString(),
                        onChange = { inventory = inventory.copy(meleePrize = it) },
                        "Melee prize",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)


                    )
                    CustomTextField(
                        value = inventory.prMelee.toString(),
                        onChange = { inventory = inventory.copy(prMelee = it) },
                        "prMelee",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomTextField(
                        value = inventory.stonePrize.toString(),
                        onChange = { inventory = inventory.copy(stonePrize = it) },
                        "Stone prize",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)


                    )
                    CustomTextField(
                        value = inventory.prStone.toString(),
                        onChange = { inventory = inventory.copy(prStone = it) },
                        "prStone",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                CustomTextField(
                    value = inventory.orderInstructions,
                    onChange = { inventory = inventory.copy(orderInstructions = it) },
                    "Order instructions",
                )
                CustomTextField(
                    value = inventory.price.toString(),
                    onChange = { inventory = inventory.copy(price = it) },
                    "Price",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("On website")
                    Checkbox(
                        checked = onWebsite,
                        onCheckedChange = { newVal -> onWebsite = newVal })
                }
            }
        }
        Column() {

            Button(onClick = { scope.launch { insertInventory(inventory) } }) {
                Text(text = "Create product")
            }
            Row() {
                var code_to_copy by remember { mutableStateOf("") }

                TextField(
                    value = code_to_copy,
                    onValueChange = { newCode -> code_to_copy = newCode })
                Button(onClick = {
                    scope.launch {
                        val inv = getProduct(code_to_copy)
                        if (inv != null) {
                            inventory = inv.copy()

                        }
//                            inventory = inventory.copy(inventoryCode = lastInv.inventoryCode)
                    }
                }) {
                    Text("Copy product")
                }
            }
        }
    }
}


@Composable
fun CustomTextField(
    value: String,
    onChange: (newVal: String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = { newVal: String -> onChange(newVal) },
        label = {
            Text(label)
        },
        keyboardOptions = keyboardOptions,
        modifier = modifier,
    )
}