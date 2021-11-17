package au.com.wpay.sdk.paymentsimulator.ui.components

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@ExperimentalMaterialApi
@Composable
fun ComboBox(
    items: List<String>,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItemText by remember { mutableStateOf(items[0]) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selectedItemText,
            onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            items.forEachIndexed { index, selectionItem ->
                DropdownMenuItem(
                    onClick = {
                        selectedItemText = selectionItem
                        expanded = false

                        onClick(index)
                    }
                ) {
                    Text(text = selectionItem)
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Preview
@Composable
fun ComboBoxPreview() {
    ComboBox(
        items = listOf("Item 1", "Item 2")
    )
}