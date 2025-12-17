package com.vishalk.rssbstream

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Trace
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.vishalk.rssbstream.data.preferences.AppThemeMode
import com.vishalk.rssbstream.data.preferences.NavBarStyle
import com.vishalk.rssbstream.data.preferences.UserPreferencesRepository
import com.vishalk.rssbstream.data.service.MusicService
import com.vishalk.rssbstream.presentation.components.DismissUndoBar
import com.vishalk.rssbstream.presentation.components.MiniPlayerBottomSpacer
import com.vishalk.rssbstream.presentation.components.MiniPlayerHeight
import com.vishalk.rssbstream.presentation.components.NavBarContentHeight
import com.vishalk.rssbstream.presentation.components.NavBarContentHeightFullWidth
import com.vishalk.rssbstream.presentation.components.PlayerInternalNavigationBar
import com.vishalk.rssbstream.presentation.components.UnifiedPlayerSheet
import com.vishalk.rssbstream.presentation.navigation.AppNavigation
import com.vishalk.rssbstream.presentation.navigation.RssbScreen
import com.vishalk.rssbstream.presentation.navigation.Screen
import com.vishalk.rssbstream.presentation.screens.SetupScreen
import com.vishalk.rssbstream.presentation.viewmodel.MainViewModel
import com.vishalk.rssbstream.presentation.viewmodel.PlayerViewModel
import com.vishalk.rssbstream.ui.theme.RssbStreamTheme
import com.vishalk.rssbstream.utils.LogUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.persistentListOf
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@Immutable
data class BottomNavItem(
    val label: String,
    @DrawableRes val iconResId: Int,
    @DrawableRes val selectedIconResId: Int? = null,
    val rssbScreen: RssbScreen // Changed from Screen to RssbScreen for type safety, though we could use a sealed interface
)

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        LogUtils.d(this, "onCreate")
        installSplashScreen()
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val systemDarkTheme = isSystemInDarkTheme()
            val appThemeMode by userPreferencesRepository.appThemeModeFlow.collectAsState(initial = AppThemeMode.FOLLOW_SYSTEM)
            val useDarkTheme = when (appThemeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                else -> systemDarkTheme
            }
            val isSetupComplete by mainViewModel.isSetupComplete.collectAsState()
            var showSetupScreen by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(isSetupComplete) {
                if (showSetupScreen == null) {
                    showSetupScreen = !isSetupComplete
                }
            }

            RssbStreamTheme(
                darkTheme = useDarkTheme
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (showSetupScreen != null) {
                        AnimatedContent(
                            targetState = showSetupScreen,
                            transitionSpec = {
                                if (targetState == false) {
                                    // Transition from Setup to Main App
                                    scaleIn(initialScale = 0.8f, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) togetherWith
                                            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
                                } else {
                                    // Placeholder for other transitions, e.g., Main App to Setup
                                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                                }
                            },
                            label = "SetupTransition"
                        ) { targetState ->
                            if (targetState == true) {
                                SetupScreen(onSetupComplete = { showSetupScreen = false })
                            } else {
                                MainAppContent(playerViewModel, mainViewModel)
                            }
                        }
                    }
                }
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        when {
            intent.getBooleanExtra("ACTION_SHOW_PLAYER", false) -> {
                playerViewModel.showPlayer()
            }

            intent.action == Intent.ACTION_VIEW && intent.data != null -> {
                intent.data?.let { uri ->
                    persistUriPermissionIfNeeded(intent, uri)
                    playerViewModel.playExternalUri(uri)
                }
                clearExternalIntentPayload(intent)
            }

            intent.action == Intent.ACTION_SEND && intent.type?.startsWith("audio/") == true -> {
                resolveStreamUri(intent)?.let { uri ->
                    persistUriPermissionIfNeeded(intent, uri)
                    playerViewModel.playExternalUri(uri)
                }
                clearExternalIntentPayload(intent)
            }
        }
    }

    private fun resolveStreamUri(intent: Intent): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.let { return it }
        } else {
            @Suppress("DEPRECATION")
            val legacyUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (legacyUri != null) return legacyUri
        }

        intent.clipData?.let { clipData ->
            if (clipData.itemCount > 0) {
                return clipData.getItemAt(0).uri
            }
        }

        return intent.data
    }

    @SuppressLint("WrongConstant")
    private fun persistUriPermissionIfNeeded(intent: Intent, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val hasPersistablePermission = (intent.flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) != 0
            if (hasPersistablePermission) {
                val takeFlags = intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                if (takeFlags != 0) {
                    try {
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                    } catch (securityException: SecurityException) {
                        Log.w("MainActivity", "Unable to persist URI permission for $uri", securityException)
                    } catch (illegalArgumentException: IllegalArgumentException) {
                        Log.w("MainActivity", "Persistable URI permission not granted for $uri", illegalArgumentException)
                    }
                }
            }
        }
    }

    private fun clearExternalIntentPayload(intent: Intent) {
        intent.data = null
        intent.clipData = null
        intent.removeExtra(Intent.EXTRA_STREAM)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @Composable
    private fun MainAppContent(playerViewModel: PlayerViewModel, mainViewModel: MainViewModel) {
        Trace.beginSection("MainActivity.MainAppContent")
        val navController = rememberNavController()

        Box(modifier = Modifier.fillMaxSize()) {
            MainUI(playerViewModel, navController)
        }
        Trace.endSection()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @Composable
    private fun MainUI(playerViewModel: PlayerViewModel, navController: NavHostController) {
        Trace.beginSection("MainActivity.MainUI")

        val commonNavItems = remember {
            persistentListOf(
                BottomNavItem("Home", R.drawable.rounded_home_24, R.drawable.home_24_rounded_filled, RssbScreen.RssbHome),
                BottomNavItem("Search", R.drawable.rounded_search_24, R.drawable.rounded_search_24, RssbScreen.Search),
                BottomNavItem("Library", R.drawable.rounded_library_music_24, R.drawable.round_library_music_24, RssbScreen.Library)
            )
        }
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val routesWithHiddenNavigationBar = remember {
            setOf(
                RssbScreen.Settings.route,
                RssbScreen.NavBarCornerRadius.route,
                RssbScreen.About.route,
                Screen.Settings.route,
                Screen.PlaylistDetail.route,
                Screen.DailyMixScreen.route,
                Screen.GenreDetail.route,
                Screen.AlbumDetail.route,
                Screen.ArtistDetail.route,
                Screen.DJSpace.route,
                Screen.NavBarCrRad.route,
                Screen.About.route,
                Screen.Stats.route,
                Screen.EditTransition.route
            )
        }
        val shouldHideNavigationBar by remember(currentRoute) {
            derivedStateOf {
                currentRoute?.let { route ->
                    routesWithHiddenNavigationBar.any { hiddenRoute ->
                        if (hiddenRoute.contains("{")) {
                            route.startsWith(hiddenRoute.substringBefore("{"))
                        } else {
                            route == hiddenRoute
                        }
                    }
                } ?: false
            }
        }

        val navBarStyle by playerViewModel.navBarStyle.collectAsState()

        val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        val horizontalPadding = if (navBarStyle == NavBarStyle.DEFAULT) {
            if (systemNavBarInset > 30.dp) 14.dp else systemNavBarInset
        } else {
            0.dp
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!shouldHideNavigationBar) {
                    val playerContentExpansionFraction = playerViewModel.playerContentExpansionFraction.value
                    val showPlayerContentArea = playerViewModel.stablePlayerState.collectAsState().value.currentSong != null
                    val currentSheetContentState by playerViewModel.sheetState.collectAsState()
                    val navBarCornerRadius by playerViewModel.navBarCornerRadius.collectAsState()
                    val navBarElevation = 3.dp

                    val playerContentActualBottomRadiusTargetValue by remember(
                        navBarStyle,
                        showPlayerContentArea,
                        playerContentExpansionFraction,
                    ) {
                        derivedStateOf {
                            if (navBarStyle == NavBarStyle.FULL_WIDTH) {
                                return@derivedStateOf lerp(32.dp, 26.dp, playerContentExpansionFraction)
                            }

                            if (showPlayerContentArea) {
                                if (playerContentExpansionFraction < 0.2f) {
                                    lerp(12.dp, 26.dp, (playerContentExpansionFraction / 0.2f).coerceIn(0f, 1f))
                                } else {
                                    26.dp
                                }
                            } else {
                                navBarCornerRadius.dp
                            }
                        }
                    }

                    val playerContentActualBottomRadius by animateDpAsState(
                        targetValue = playerContentActualBottomRadiusTargetValue,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                        ),
                        label = "PlayerContentBottomRadius"
                    )

                    val navBarHideFraction = if (showPlayerContentArea) playerContentExpansionFraction else 0f
                    val navBarHideFractionClamped = navBarHideFraction.coerceIn(0f, 1f)

                    val actualShape = remember(playerContentActualBottomRadius, showPlayerContentArea, navBarStyle, navBarCornerRadius) {
                        val bottomRadius = if (navBarStyle == NavBarStyle.FULL_WIDTH) 0.dp else navBarCornerRadius.dp
                        AbsoluteSmoothCornerShape(
                            cornerRadiusTL = playerContentActualBottomRadius,
                            smoothnessAsPercentBR = 60,
                            cornerRadiusTR = playerContentActualBottomRadius,
                            smoothnessAsPercentTL = 60,
                            cornerRadiusBL = bottomRadius,
                            smoothnessAsPercentTR = 60,
                            cornerRadiusBR = bottomRadius,
                            smoothnessAsPercentBL = 60
                        )
                    }

                    val bottomBarPadding = if (navBarStyle == NavBarStyle.FULL_WIDTH) 0.dp else systemNavBarInset

                    var componentHeightPx by remember { mutableIntStateOf(0) }
                    val density = LocalDensity.current
                    val shadowOverflowPx = remember(navBarElevation, density) {
                        with(density) { (navBarElevation * 8).toPx() }
                    }
                    val bottomBarPaddingPx = remember(bottomBarPadding, density) {
                        with(density) { bottomBarPadding.toPx() }
                    }
                    val animatedTranslationY by remember(
                        navBarHideFractionClamped,
                        componentHeightPx,
                        shadowOverflowPx,
                        bottomBarPaddingPx,
                    ) {
                        derivedStateOf {
                            (componentHeightPx.toFloat() + shadowOverflowPx + bottomBarPaddingPx) * navBarHideFractionClamped
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = bottomBarPadding)
                            .onSizeChanged { componentHeightPx = it.height }
                            .graphicsLayer {
                                translationY = animatedTranslationY
                                alpha = 1f
                            }
                    ) {
                        val navHeight: Dp = if (navBarStyle == NavBarStyle.DEFAULT) {
                            NavBarContentHeight
                        } else {
                            NavBarContentHeightFullWidth + systemNavBarInset
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(navHeight)
                                .padding(horizontal = horizontalPadding),
                            color = NavigationBarDefaults.containerColor,
                            shape = actualShape,
                            shadowElevation = navBarElevation
                        ) {
                            // Convert BottomNavItem(RssbScreen) to expected BottomNavItem(Screen) for compatibility
                            // Note: We need to adapt PlayerInternalNavigationBar to handle RssbScreen or use string routes
                            PlayerInternalNavigationBar(
                                navController = navController,
                                navItems = commonNavItems,
                                currentRoute = currentRoute,
                                navBarStyle = navBarStyle,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val density = LocalDensity.current
                val configuration = LocalWindowInfo.current
                val screenHeightPx = remember(configuration) { with(density) { configuration.containerSize.height } }
                val containerHeight = this.maxHeight

                val stablePlayerState by playerViewModel.stablePlayerState.collectAsState()
                val showPlayerContentInitially = stablePlayerState.currentSong != null

                val routesWithHiddenMiniPlayer = remember {
                    setOf(
                        Screen.NavBarCrRad.route,
                        RssbScreen.NavBarCornerRadius.route
                    )
                }
                val shouldHideMiniPlayer by remember(currentRoute) {
                    derivedStateOf { currentRoute in routesWithHiddenMiniPlayer }
                }

                val miniPlayerH = with(density) { MiniPlayerHeight.toPx() }
                val totalSheetHeightWhenContentCollapsedPx = if (showPlayerContentInitially && !shouldHideMiniPlayer) miniPlayerH else 0f

                val bottomMargin = innerPadding.calculateBottomPadding()

                val spacerPx = with(density) { MiniPlayerBottomSpacer.toPx() }
                val sheetCollapsedTargetY = screenHeightPx - totalSheetHeightWhenContentCollapsedPx - with(density){ bottomMargin.toPx() } - spacerPx

                AppNavigation(
                    playerViewModel = playerViewModel,
                    navController = navController,
                    paddingValues = innerPadding,
                    userPreferencesRepository = userPreferencesRepository
                )

                UnifiedPlayerSheet(
                    playerViewModel = playerViewModel,
                    sheetCollapsedTargetY = sheetCollapsedTargetY,
                    collapsedStateHorizontalPadding = horizontalPadding,
                    hideMiniPlayer = shouldHideMiniPlayer,
                    containerHeight = containerHeight,
                    navController = navController,
                    isNavBarHidden = shouldHideNavigationBar
                )

                val playerUiState by playerViewModel.playerUiState.collectAsState()

                AnimatedVisibility(
                    visible = playerUiState.showDismissUndoBar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = innerPadding.calculateBottomPadding() + MiniPlayerBottomSpacer)
                        .padding(horizontal = horizontalPadding)
                ) {
                    DismissUndoBar(
                        modifier = Modifier
                            .fillMaxWidth()
//                            .background(
//                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
//                                shape = CircleShape
//                            )
                            .height(MiniPlayerHeight)
                            .padding(horizontal = 14.dp),
                        onUndo = { playerViewModel.undoDismissPlaylist() },
                        onClose = { playerViewModel.hideDismissUndoBar() },
                        durationMillis = playerUiState.undoBarVisibleDuration
                    )
                }
            }
        }
        Trace.endSection()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        LogUtils.d(this, "onStart")
        playerViewModel.onMainActivityStart()
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        super.onStop()
        LogUtils.d(this, "onStop")
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }

    override fun onResume() {
        super.onResume()
        // Sync is removed as per requirement
    }


}
