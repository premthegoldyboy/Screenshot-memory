package com.example.screenshotmemory.ui.screens.viewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotViewerScreen(
    viewModel: ScreenshotViewerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val screenshot by viewModel.screenshot.collectAsStateWithLifecycle()

    val animatedScale = remember { Animatable(1f) }
val animatedOffsetX = remember { Animatable(0f) }
val animatedOffsetY = remember { Animatable(0f) }

var gestureScale by remember { mutableFloatStateOf(1f) }
var gestureOffset by remember { mutableStateOf(Offset.Zero) }

LaunchedEffect(Unit) {
    animatedScale.snapTo(1f)
    animatedOffsetX.snapTo(0f)
    animatedOffsetY.snapTo(0f)
}
    
    var noteText by remember(screenshot?.id) { mutableStateOf(screenshot?.notes ?: "") }
    var tagText by remember(screenshot?.id) { mutableStateOf(screenshot?.tags ?: "") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOcrBottomSheet by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(screenshot) {
        noteText = screenshot?.notes ?: ""
        tagText = screenshot?.tags ?: ""
    }

    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = screenshot?.filename ?: "Screenshot Viewer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        screenshot?.let {
                            Text(
                                text = dateFormatter.format(Date(it.dateTaken)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("viewer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            screenshot?.let { item ->
                                val updated = item.copy(isFavorite = !item.isFavorite)
                                viewModel.updateScreenshot(updated)
                            }
                        },
                        modifier = Modifier.testTag("viewer_favorite_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (screenshot?.isFavorite == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showOcrBottomSheet = true },
                        modifier = Modifier.testTag("view_ocr_text_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "Extracted Text"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            screenshot?.let { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Open Original
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(item.uri), "image/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open image viewer", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("open_original_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open Original")
                        }

                        // Share
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_STREAM, Uri.parse(item.uri))
                                        type = "image/*"
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Screenshot"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot share image", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("share_screenshot_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }

                        // OCR Text
                        IconButton(
                            onClick = { showOcrBottomSheet = true },
                            modifier = Modifier.testTag("ocr_text_bottom_button")
                        ) {
                            Icon(Icons.Default.TextFields, contentDescription = "View Text")
                        }

                        // Delete
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_screenshot_button")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            screenshot?.let { item ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(item.uri))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.filename,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
    .fillMaxSize()
    .pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->

            gestureScale = (gestureScale * zoom).coerceIn(1f, 5f)

            if (gestureScale > 1f) {
                gestureOffset = Offset(
                    x = gestureOffset.x + pan.x,
                    y = gestureOffset.y + pan.y
                )

                animatedScale.snapTo(gestureScale)
                animatedOffsetX.snapTo(gestureOffset.x)
                animatedOffsetY.snapTo(gestureOffset.y)
            } else {
                gestureScale = 1f
                gestureOffset = Offset.Zero

                animatedScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )

                animatedOffsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )

                animatedOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }
    .graphicsLayer {
        scaleX = animatedScale.value
        scaleY = animatedScale.value
        translationX = animatedOffsetX.value
        translationY = animatedOffsetY.value
    }
                )
            }
        }

        screenshot?.let { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Notes & Tags",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Notes") },
                        minLines = 3,
                        maxLines = 5
                    )

                    OutlinedTextField(
                        value = tagText,
                        onValueChange = { tagText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tags") },
                        placeholder = { Text("work, travel, bills") }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                noteText = item.notes
                                tagText = item.tags
                            }
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                val updated = item.copy(notes = noteText.trim(), tags = tagText.trim())
                                viewModel.updateScreenshot(updated)
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        // OCR Text Bottom Sheet
        if (showOcrBottomSheet && screenshot != null) {
            val text = screenshot!!.ocrText
            ModalBottomSheet(
                onDismissRequest = { showOcrBottomSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recognized OCR Text",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        if (text.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("OCR Text", text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("copy_ocr_text_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Text")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (text.isBlank()) {
                        Text(
                            text = "No readable text extracted from this screenshot.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showOcrBottomSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Remove Screenshot?") },
                text = { Text("This will remove this screenshot from your Screenshot Memory index. Your original phone file will not be altered.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteScreenshot { onBack() }
                        },
                        modifier = Modifier.testTag("confirm_delete_button")
                    ) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
