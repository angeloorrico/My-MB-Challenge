package com.aorrico.mymbchallenge.feature.exchangedetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.aorrico.mymbchallenge.core.ui.components.ExchangeLogo
import com.aorrico.mymbchallenge.core.ui.format.formatFeePercent
import com.aorrico.mymbchallenge.core.ui.format.formatLaunchDate
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import com.aorrico.mymbchallenge.feature.exchangedetail.R

@Composable
fun ExchangeDetailHeader(detail: ExchangeDetail, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    // Single root layout node: this is placed inside a LazyColumn `item { }` slot, so it must
    // not emit multiple sibling layout nodes at the top level.
    Column(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                ExchangeLogo(
                    logoUrl = detail.logoUrl,
                    contentDescription = detail.name,
                    modifier = Modifier.size(64.dp),
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(text = detail.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = stringResource(R.string.detail_id_format, detail.id),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val description = detail.description
            if (description != null) {
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
            }

            val websiteUrl = detail.websiteUrl
            if (websiteUrl != null) {
                TextButton(
                    onClick = { uriHandler.openUri(websiteUrl) },
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = websiteUrl,
                        textDecoration = TextDecoration.Underline,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            InfoRow(label = stringResource(R.string.detail_maker_fee), value = formatFeePercent(detail.makerFee))
            InfoRow(label = stringResource(R.string.detail_taker_fee), value = formatFeePercent(detail.takerFee))
            InfoRow(label = stringResource(R.string.detail_launched), value = formatLaunchDate(detail.dateLaunched))

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.detail_assets_section_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
