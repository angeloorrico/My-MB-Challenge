package com.aorrico.mymbchallenge.feature.exchangelist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aorrico.mymbchallenge.core.ui.components.ExchangeLogo
import com.aorrico.mymbchallenge.core.ui.theme.MyMbChallengeTheme
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.feature.exchangelist.R
import java.time.Instant

@Composable
fun RecentlyViewedRow(
    exchanges: List<RecentlyViewedExchange>,
    onExchangeClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.recently_viewed_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(count = exchanges.size, key = { exchanges[it].exchangeId }) { index ->
                val exchange = exchanges[index]
                RecentlyViewedItem(
                    exchange = exchange,
                    onClick = { onExchangeClick(exchange.exchangeId) },
                )
            }
        }
    }
}

@Composable
private fun RecentlyViewedItem(exchange: RecentlyViewedExchange, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExchangeLogo(
            logoUrl = exchange.logoUrl,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = exchange.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecentlyViewedRowPreview() {
    MyMbChallengeTheme {
        RecentlyViewedRow(
            exchanges = listOf(
                RecentlyViewedExchange(270, "Binance", null, Instant.now()),
                RecentlyViewedExchange(24, "Coinbase Exchange", null, Instant.now()),
            ),
            onExchangeClick = {},
        )
    }
}
