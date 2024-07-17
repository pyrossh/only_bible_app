package dev.pyrossh.onlyBible

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Parcelable
import android.text.Html
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.microsoft.cognitiveservices.speech.SpeechSynthesisEventArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class ChapterScreenProps(
    val bookIndex: Int,
    val chapterIndex: Int,
    // TODO: fix this
    val dir: String = Dir.Left.name,
) : Parcelable

@Parcelize
enum class Dir : Parcelable {
    Left, Right;

    fun slideDirection(): AnimatedContentTransitionScope.SlideDirection {
        return when (this) {
            Left -> AnimatedContentTransitionScope.SlideDirection.Left
            Right -> AnimatedContentTransitionScope.SlideDirection.Right
        }
    }

    fun reverse(): Dir {
        return if (this == Left) Right else Left
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun ChapterScreen(
    model: AppViewModel,
    bookIndex: Int,
    chapterIndex: Int,
    navController: NavController,
    openDrawer: (MenuType, Int) -> Job,
) {
    val context = LocalContext.current
    val darkTheme = model.isDarkTheme(isSystemInDarkTheme())
    val fontType = FontType.valueOf(model.fontType)
    val fontSizeDelta = model.fontSizeDelta
    val boldWeight = if (model.fontBoldEnabled) FontWeight.W700 else FontWeight.W400
    val scope = rememberCoroutineScope()
    var selectedVerses by rememberSaveable {
        mutableStateOf(listOf<Verse>())
    }
    var isPlaying by rememberSaveable {
        mutableStateOf(false)
    }
    var dragAmount by remember {
        mutableFloatStateOf(0.0f)
    }
    DisposableEffect(Unit) {
        val started = { _: Any, _: SpeechSynthesisEventArgs ->
            isPlaying = true
        }
        val completed = { _: Any, _: SpeechSynthesisEventArgs ->
            isPlaying = false
        }
        model.speechService.SynthesisStarted.addEventListener(started)
        model.speechService.SynthesisCompleted.addEventListener(completed)

        onDispose {
            model.speechService.SynthesisStarted.removeEventListener(started)
            model.speechService.SynthesisCompleted.removeEventListener(completed)
        }
    }
    val chapterVerses =
        model.verses.filter { it.bookIndex == bookIndex && it.chapterIndex == chapterIndex }
    val headingColor = MaterialTheme.colorScheme.onSurface // MaterialTheme.colorScheme.primary,
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .height(72.dp),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.clickable {
                                openDrawer(MenuType.Book, bookIndex)
                            },
                            text = model.bookNames[bookIndex],
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W500,
                                color = headingColor,
                            )
                        )
                        TextButton(onClick = { openDrawer(MenuType.Chapter, bookIndex) }) {
                            Text(
                                text = "${chapterIndex + 1}",
                                style = TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.W500,
                                    color = headingColor,
                                )
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { openDrawer(MenuType.Bible, bookIndex) }) {
                        Text(
                            text = model.bibleName.substring(0, 2).uppercase(),
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W500,
                                color = headingColor,
                            ),
                        )
                    }
                    TextButton(
                        onClick = {
                            model.showSheet()
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More",
                            tint = headingColor,
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (selectedVerses.isNotEmpty()) {
                BottomAppBar(
                    containerColor = Color.Transparent,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .height(104.dp),
                    actions = {
                        Surface(
                            color = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .height(1.dp)
                                    .padding(bottom = 12.dp)
                                    .fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = {
                                    selectedVerses = listOf()
                                }) {
                                    Icon(
                                        modifier = Modifier.size(36.dp),
                                        imageVector = Icons.Outlined.Cancel,
                                        contentDescription = "Clear",
                                    )
                                }
                                IconButton(onClick = {
                                    if (isPlaying) {
                                        model.speechService.StopSpeakingAsync()
                                    } else {
                                        scope.launch(Dispatchers.IO) {
                                            for (v in selectedVerses.sortedBy { it.verseIndex }) {
                                                model.speechService.StartSpeakingSsml(v.toSSML())
                                            }
                                        }
                                    }
                                }) {
                                    Icon(
                                        modifier = Modifier.size(36.dp),
                                        imageVector = if (isPlaying)
                                            Icons.Outlined.PauseCircle
                                        else
                                            Icons.Outlined.PlayCircle,
                                        contentDescription = "Audio",
                                    )
                                }
//                                IconButton(onClick = {
//                                    shareVerses(context, selectedVerses)
//                                }) {
//                                    Icon(
//                                        modifier = Modifier.size(48.dp),
//                                        imageVector = Icons.Outlined.Edit,
//                                        contentDescription = "Highlight",
//                                    )
//                                }
                                IconButton(onClick = {
                                    shareVerses(
                                        context,
                                        selectedVerses.sortedBy { it.verseIndex })
                                }) {
                                    Icon(
                                        modifier = Modifier.size(32.dp),
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Share",
                                    )
                                }
                            }
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(
//                                Icons.Filled.Circle,
//                                contentDescription = "",
//                                modifier = Modifier.size(64.dp),
//                                tint = Color.Yellow
//                            )
//                        }
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(
//                                Icons.Filled.Circle,
//                                contentDescription = "",
//                                modifier = Modifier.size(64.dp),
//                                tint = Color.Blue,
//                            )
//                        }
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(
//                                Icons.Filled.Circle,
//                                contentDescription = "",
//                                modifier = Modifier.size(64.dp),
//                                tint = Color.Cyan,
//                            )
//                        }
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(
//                                Icons.Filled.Circle,
//                                contentDescription = "",
//                                modifier = Modifier.size(64.dp),
//                                tint = Color.Magenta,
//                            )
//                        }
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onDragEnd = {
//                            println("END " + dragAmount);
                        if (dragAmount < 0) {
                            val pair = Verse.getForwardPair(bookIndex, chapterIndex)
                            navController.navigate(
                                ChapterScreenProps(
                                    bookIndex = pair.first,
                                    chapterIndex = pair.second,
                                )
                            )
                        } else if (dragAmount > 0) {
                            val pair = Verse.getBackwardPair(bookIndex, chapterIndex)
                            if (navController.previousBackStackEntry != null) {
                                val previousBook =
                                    navController.previousBackStackEntry?.arguments?.getInt("book")
                                        ?: 0
                                val previousChapter =
                                    navController.previousBackStackEntry?.arguments?.getInt("chapter")
                                        ?: 0
//                                    println("currentBackStackEntry ${previousBook} ${previousChapter} || ${pair.first} ${pair.second}")
                                if (previousBook == pair.first && previousChapter == pair.second) {
                                    println("Popped")
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(
                                        ChapterScreenProps(
                                            bookIndex = pair.first,
                                            chapterIndex = pair.second,
                                            dir = Dir.Right.name,
                                        )
                                    )
                                }
                            } else {
//                                    println("navigated navigate")
                                navController.navigate(
                                    ChapterScreenProps(
                                        bookIndex = pair.first,
                                        chapterIndex = pair.second,
                                        dir = Dir.Right.name
                                    )
                                )
                            }
                        }
                    }, onHorizontalDrag = { change, da ->
                        dragAmount = da
                        change.consume()
                    })
                }) {
            items(chapterVerses) { v ->
                if (v.heading.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(
                            top = if (v.verseIndex != 0) 12.dp else 0.dp, bottom = 12.dp
                        ),
                        style = TextStyle(
                            fontFamily = fontType.family(),
                            fontSize = (16 + fontSizeDelta).sp,
                            fontWeight = FontWeight.W700,
                            color = headingColor,
                        ),
                        text = v.heading.replace("<br>", "\n")
                    )
                }
                val isSelected = selectedVerses.contains(v);
                val buttonInteractionSource = remember { MutableInteractionSource() }
                Text(
                    modifier = Modifier
                        .clickable(
                            interactionSource = buttonInteractionSource,
                            indication = null
                        ) {
                            selectedVerses = if (selectedVerses.contains(v)) {
                                selectedVerses - v
                            } else {
                                selectedVerses + v
                            }
                        },
                    style = TextStyle(
                        background = if (isSelected)
                            MaterialTheme.colorScheme.outline
                        else
                            Color.Unspecified,
                        fontFamily = fontType.family(),
                        color = if (darkTheme)
                            Color(0xFFBCBCBC)
                        else
                            Color(0xFF000104),
                        fontWeight = boldWeight,
                        fontSize = (17 + fontSizeDelta).sp,
                        lineHeight = (23 + fontSizeDelta).sp,
                        letterSpacing = 0.sp,
                    ),
                    text = buildAnnotatedString {
                        val spanned = Html.fromHtml(v.text, Html.FROM_HTML_MODE_COMPACT)
                        val spans = spanned.getSpans(0, spanned.length, Any::class.java)
                        val verseNo = "${v.verseIndex + 1} "
                        withStyle(
                            style = SpanStyle(
                                fontSize = (13 + fontSizeDelta).sp,
                                color = if (darkTheme)
                                    Color(0xFFCCCCCC)
                                else Color(0xFFA20101),
                                fontWeight = FontWeight.W700,
                            )
                        ) {
                            append(verseNo)
                        }
                        append(spanned.toString())
                        spans
                            .filter { it !is BulletSpan }
                            .forEach { span ->
                                val start = spanned.getSpanStart(span)
                                val end = spanned.getSpanEnd(span)
                                when (span) {
                                    is ForegroundColorSpan ->
                                        if (darkTheme) SpanStyle(color = Color(0xFFFF636B))
                                        else SpanStyle(color = Color(0xFFFF0000))

                                    is StyleSpan -> when (span.style) {
                                        Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                                        Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                                        Typeface.BOLD_ITALIC -> SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic,
                                        )

                                        else -> null
                                    }

                                    else -> {
                                        null
                                    }
                                }?.let { spanStyle ->
                                    addStyle(
                                        spanStyle,
                                        start + verseNo.length - 1,
                                        end + verseNo.length
                                    )
                                }
                            }
                    }
                )
            }
        }
    }
}