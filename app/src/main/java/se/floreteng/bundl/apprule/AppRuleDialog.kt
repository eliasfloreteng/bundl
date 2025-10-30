package se.floreteng.bundl.apprule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddAppRuleDialog(
    state: AppRuleState, onEvent: (AppRuleEvent) -> Unit, modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = {
            onEvent(AppRuleEvent.HideDialog)
        },
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = state.packageName,
                    onValueChange = {
                        onEvent(AppRuleEvent.SetPackageName(it))
                    },
                    placeholder = {
                        Text(text = "Package Name")
                    }
                )
                Text(text = "Mode:")
                Row {
                    Text(text = "Whitelist")
                    RadioButton(
                        selected = state.mode == AppRuleMode.WHITELIST,
                        onClick = {
                            onEvent(AppRuleEvent.SetMode(AppRuleMode.WHITELIST))
                        }
                    )
                }
                Row {
                    Text(text = "Blacklist")
                    RadioButton(
                        selected = state.mode == AppRuleMode.BLACKLIST,
                        onClick = {
                            onEvent(AppRuleEvent.SetMode(AppRuleMode.BLACKLIST))
                        }
                    )
                }
                TextField(
                    value = state.filterString ?: "",
                    onValueChange = {
                        onEvent(AppRuleEvent.SetFilterString(it))
                    },
                    placeholder = {
                        Text(text = "Filter String")
                    }
                )

                Button(
                    onClick = {
                        onEvent(AppRuleEvent.SaveAppRule)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}