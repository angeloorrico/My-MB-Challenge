package com.aorrico.mymbchallenge.feature.exchangelist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.aorrico.mymbchallenge.core.ui.components.ExchangeLogo
import com.aorrico.mymbchallenge.core.ui.format.formatCompactUsd
import com.aorrico.mymbchallenge.core.ui.format.formatLaunchDate
import com.aorrico.mymbchallenge.core.ui.theme.MyMbChallengeTheme
import com.aorrico.mymbchallenge.domain.model.Exchange
import java.time.Instant

@Composable
fun ExchangeListItem(
    exchange: Exchange,
    onClick: (Exchange) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
            .clickable { onClick(exchange) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${exchange.name}, volume ${formatCompactUsd(exchange.spotVolumeUsd)}"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExchangeLogo(
            logoUrl = exchange.logoUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = exchange.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Volume 24h: ${formatCompactUsd(exchange.spotVolumeUsd)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = formatLaunchDate(exchange.dateLaunched),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExchangeListItemPreview() {
    MyMbChallengeTheme {
        ExchangeListItem(
            exchange = Exchange(
                id = 270,
                name = "Binance",
                slug = "binance",
                logoUrl = null,
                spotVolumeUsd = 12_345_678_910.0,
                dateLaunched = Instant.parse("2017-07-14T00:00:00Z"),
            ),
            onClick = {},
        )
    }
}
