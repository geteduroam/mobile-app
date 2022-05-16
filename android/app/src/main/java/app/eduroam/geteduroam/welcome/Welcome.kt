package app.eduroam.geteduroam.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.eduroam.geteduroam.R
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Welcome() {
	AppScaffold(topBarTitle = stringResource(id = R.string.name)) { padding ->
		Column(
			Modifier
				.fillMaxSize()
				.padding(padding)
		) {
			val pagerState = rememberPagerState()

			// Display 10 items
			HorizontalPager(
				count = 10,
				state = pagerState,
				// Add 32.dp horizontal padding to 'center' the pages
				contentPadding = PaddingValues(horizontal = 32.dp),
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth(),
			) { page ->
				PagerSampleItem(
					page = page,
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(1f)
				)
			}

			HorizontalPagerIndicator(
				pagerState = pagerState,
				modifier = Modifier
					.align(Alignment.CenterHorizontally)
					.padding(16.dp),
			)

			ActionsRow(
				pagerState = pagerState,
				modifier = Modifier.align(Alignment.CenterHorizontally)
			)
		}
	}
}

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun ActionsRow(
	pagerState: PagerState,
	modifier: Modifier = Modifier,
	infiniteLoop: Boolean = false
) {
	Row(modifier) {
		val scope = rememberCoroutineScope()

		IconButton(
			enabled = infiniteLoop.not() && pagerState.currentPage > 0,
			onClick = {
				scope.launch {
					pagerState.animateScrollToPage(0)
				}
			}
		) {
			Icon(Icons.Default.FirstPage, null)
		}

		IconButton(
			enabled = infiniteLoop || pagerState.currentPage > 0,
			onClick = {
				scope.launch {
					pagerState.animateScrollToPage(pagerState.currentPage - 1)
				}
			}
		) {
			Icon(Icons.Default.NavigateBefore, null)
		}

		IconButton(
			enabled = infiniteLoop || pagerState.currentPage < pagerState.pageCount - 1,
			onClick = {
				scope.launch {
					pagerState.animateScrollToPage(pagerState.currentPage + 1)
				}
			}
		) {
			Icon(Icons.Default.NavigateNext, null)
		}

		IconButton(
			enabled = infiniteLoop.not() && pagerState.currentPage < pagerState.pageCount - 1,
			onClick = {
				scope.launch {
					pagerState.animateScrollToPage(pagerState.pageCount - 1)
				}
			}
		) {
			Icon(Icons.Default.LastPage, null)
		}
	}
}
