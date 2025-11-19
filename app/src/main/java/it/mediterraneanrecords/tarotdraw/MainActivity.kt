package it.mediterraneanrecords.tarotdraw

/* =========================================================
   IMPORTS (puliti)
   ========================================================= */
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import kotlin.math.PI
import kotlin.math.sin

import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import it.mediterraneanrecords.tarotdraw.ExperienceGate
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/* =========================================================
   LOG
   ========================================================= */
private const val TAG = "SanarTarotAI"
private fun logD(msg: String) = Log.d(TAG, msg)

private fun appLangTag(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty)
        java.util.Locale.getDefault().toLanguageTag()
    else
        (locales[0]?.toLanguageTag() ?: "it")
}

/* =================== BUILD VARIANTS =================== */
// Per ora esiste solo la versione FREE.
// Quando creeremo davvero una Pro, useremo BuildConfig o i flavors.
private val isProVersion: Boolean
    get() = false

/* =========================================================
   TEMA
   ========================================================= */
private val CustomColorScheme = lightColorScheme(
    primary = Color(0xFFC0955D),
    secondary = Color(0xFFD6B08D),
    tertiary = Color(0xFFE5CBAA)
)

@Composable
fun AppStartupAmbient(loopBase: Boolean = false) {
    val ctx = LocalContext.current
    LaunchedEffect(Unit) {
        // Se hai un file di intro: suona una volta
        if (!loopBase) {
            AmbientAudio.play(ctx, R.raw.amb_intro, loop = false)
        } else {
            // Oppure avvia subito un “base” in loop
            AmbientAudio.play(ctx, R.raw.amb_base, loop = true)
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CustomColorScheme, content = content)
}

/* =========================================================
   LLM glue (TOP-LEVEL)
   ========================================================= */
@Volatile
private var lastVisionForPrompt: String = ""

@Volatile
private var lastLangTagForPrompt: String = "it"

/* =========================================================
   VIEWMODEL
   ========================================================= */
enum class CardDisplayMode { COMPACT, FULL_IMAGE }

// inizio CLASSE TarotViewModel
// ======================== TAROT VIEWMODEL ========================
class TarotViewModel(private val state: SavedStateHandle) : ViewModel() {
// --------------------------------------------------------------
// AUDIO CARD — stato visibilità + ripristino
// --------------------------------------------------------------

    var showAudioCard by mutableStateOf(true)
        private set

    fun hideAudioCard() {
        showAudioCard = false
    }

    fun resetAudioCard() {
        showAudioCard = true
    }
    // ---- LOG ----
    private val TAG = "SanarTarotAI"

    // ---- Skin / mazzo ----
    private var _deck by mutableStateOf(state.get<String>("deck") ?: "base")
    val deck get() = _deck
    fun onDeckSkinChange(s: String) {
        _deck = s; state["deck"] = s
    }

    // ---- Stato “soft” (AI) ----
    private var _llmBoosts by mutableStateOf<LlmBoosts?>(null)
    val llmBoosts get() = _llmBoosts

    // Mood canonicalizzato + boosts locali (per UI/weights)
    private var _canonMoodText: String = ""
    val canonMoodText: String get() = _canonMoodText

    private var _localBoosts: LlmBoosts? = null
    val localBoosts: LlmBoosts? get() = _localBoosts

    // Mostra rotellina/disable pulsanti durante le chiamate AI
    private var _isAnalyzing by mutableStateOf(false)
    val isAnalyzing: Boolean get() = _isAnalyzing

    // ---- Stato “hard” ----
    private var _count by mutableIntStateOf(state["count"] ?: 3)
    private var _includeMajors by mutableStateOf(state["includeMajors"] ?: true)
    private var _includeMinors by mutableStateOf(state["includeMinors"] ?: true)
    private var _allowReversed by mutableStateOf(state["allowReversed"] ?: false)
    private var _personalNumbers by mutableStateOf(state["personalNumbers"] ?: "")
    private var _mentalImageText by mutableStateOf(state["mentalImageText"] ?: "")
    private var _visionText by mutableStateOf(state["visionText"] ?: "")
    private var _influencePct by mutableIntStateOf(state["influencePct"] ?: 50)

    // ---- Altri stati ----
    private var _noSynonymFound by mutableStateOf(false)
    val noSynonymFound get() = _noSynonymFound
    private var _moodAnalysisDone by mutableStateOf(false)
    val moodAnalysisDone: Boolean get() = _moodAnalysisDone
    private var _deterministic by mutableStateOf(state["deterministic"] ?: false)
    private var _showCreditsDialog by mutableStateOf(state["showCreditsDialog"] ?: false)
    private var _cardDisplayMode by mutableStateOf(
        state.get<CardDisplayMode>("cardDisplayMode") ?: CardDisplayMode.FULL_IMAGE
    )
    private var _drawn by mutableStateOf<MutableList<DrawnCard>>(
        state.get<ArrayList<DrawnCard>>("drawn") ?: mutableListOf()
    )

    // ---- Getter pubblici ----
    val count get() = _count
    val includeMajors get() = _includeMajors
    val includeMinors get() = _includeMinors
    val allowReversed get() = _allowReversed
    val personalNumbers get() = _personalNumbers
    val mentalImageText get() = _mentalImageText
    val visionText get() = _visionText
    val influencePct get() = _influencePct
    val deterministic get() = _deterministic
    val showCreditsDialog get() = _showCreditsDialog
    val cardDisplayMode get() = _cardDisplayMode
    val drawn: List<DrawnCard> get() = _drawn

    // ---- Mutators ----
    fun onCountChange(v: Int) {
        _count = v.coerceIn(1, 15); state["count"] = _count
    }

    fun onToggleMajors(b: Boolean) {
        _includeMajors = b; state["includeMajors"] = b
    }

    fun onToggleMinors(b: Boolean) {
        _includeMinors = b; state["includeMinors"] = b
    }

    fun onToggleReversed(b: Boolean) {
        _allowReversed = b; state["allowReversed"] = b
    }

    fun onPersonalNumbersChange(v: String) {
        _personalNumbers = v; state["personalNumbers"] = v
    }

    fun onMentalImageChange(v: String) {
        _mentalImageText = v
        state["mentalImageText"] = v
        // Sto digitando: non mostrare avvisi residui e segna analisi non conclusa
        _noSynonymFound = false
        _moodAnalysisDone = false
    }

    fun onVisionChange(v: String) {
        _visionText = v
        state["visionText"] = v
        MoodEngine.updateSuggestedMajorsFromText(v, appLangTag())
    }

    fun onInfluencePctChange(v: Int) {
        _influencePct = v.coerceIn(0, 100); state["influencePct"] = _influencePct
    }

    fun onToggleDeterministic(b: Boolean) {
        _deterministic = b; state["deterministic"] = b
    }

    fun onShowCreditsDialog(show: Boolean) {
        _showCreditsDialog = show; state["showCreditsDialog"] = show
    }

    fun onToggleCardDisplay() {
        _cardDisplayMode = if (_cardDisplayMode == CardDisplayMode.COMPACT)
            CardDisplayMode.FULL_IMAGE else CardDisplayMode.COMPACT
        state["cardDisplayMode"] = _cardDisplayMode
    }

    // ---- Storage ----
    fun loadFromStorage(ctx: Context) {
        viewModelScope.launch {
            runCatching {
                val (savedCount, savedDraw) = Prefs.load(ctx)
                if (savedDraw.isNotEmpty()) {
                    _count = savedCount
                    _drawn = savedDraw.toMutableList()
                    state["count"] = _count
                    state["drawn"] = ArrayList(_drawn)
                }
            }
        }
    }

    fun refreshLlmBoosts(ctx: Context, langTag: String) {
        val moodRaw = _mentalImageText.trim()
        val apiKey = ""
        if (apiKey.isBlank()) {
            _canonMoodText = ""
            _localBoosts = null
            _llmBoosts = null
            _noSynonymFound = false
            _moodAnalysisDone = false
            return
        }
        if (moodRaw.isEmpty()) {
            _canonMoodText = ""
            _localBoosts = null
            _llmBoosts = null
            _noSynonymFound = false
            _moodAnalysisDone = false
            return
        }

        viewModelScope.launch {
            _isAnalyzing = true

            // 1) Canonicalizza (locale → cache → Datamuse → opz. OpenAI)
            val canon = runCatching {
                MoodEngine.canonicalizeMoodText(
                    text = moodRaw,
                    ctx = ctx,
                    langTag = langTag,
                    useDatamuse = true,
                    useOpenAI = apiKey.isNotBlank(),
                    openAiKey = apiKey
                )
            }.getOrDefault(moodRaw)

            // 2) Esiste almeno UNA parola riconoscibile?
            val hasKnown = runCatching {
                MoodEngine.hasAnyKnownWord(
                    text = canon,
                    ctx = ctx,
                    langTag = langTag,
                    useDatamuse = true,
                    useOpenAI = apiKey.isNotBlank(),
                    openAiKey = apiKey
                )
            }.getOrDefault(false)

            // 3) Analisi locale (CPU)
            val local = withContext(Dispatchers.Default) {
                MoodEngine.analyzeMoodLocally(canon, ctx)
            }

            // 4) (facoltativo) Boost LLM remoto (IO)
            val remote: LlmBoosts? = if (apiKey.isNotBlank()) {
                val prompt = MoodEngine.buildLlmPrompt(canon)
                withContext(Dispatchers.IO) {
                    runCatching { LlmClient.fetchBoostsFromOpenAI(apiKey, prompt) }.getOrNull()
                }
            } else null

            // 5) Aggiorna stato UI
            withContext(Dispatchers.Main) {
                _canonMoodText = canon
                _localBoosts = local
                _llmBoosts = remote ?: local

                val b = _llmBoosts
                val anyBoosted = b != null && (
                        b.majors != 1f || b.wands != 1f || b.cups != 1f || b.swords != 1f || b.pentacles != 1f
                        )

                _noSynonymFound = !hasKnown && !anyBoosted
                _moodAnalysisDone = true

                MoodEngine.updateSuggestedMajorsFromText(canon, langTag)
                Log.d(
                    TAG,
                    "Mood -> canon='$canon', hasKnown=$hasKnown, noSyn=$_noSynonymFound, boosts=$_llmBoosts"
                )
                _isAnalyzing = false
            }
        }
    }
    // =================== END LLM / MOOD ===================

    // ===== RNG / PESI / ESTRAZIONE =====
    private fun rng(): kotlin.random.Random {
        if (!_deterministic) return kotlin.random.Random(System.currentTimeMillis())
        val key = "$_personalNumbers|$_mentalImageText|$_visionText".ifBlank { "default" }
        return kotlin.random.Random(key.hashCode().toLong())
    }

    private fun weightsFor(pool: List<TarotCard>): FloatArray {
        if (pool.isEmpty()) return FloatArray(0)
        val w = FloatArray(pool.size) { 1f }

        val text = _canonMoodText.ifBlank { _mentalImageText.trim() }
        val boosts = _llmBoosts
        val alpha = (_influencePct.coerceIn(0, 100)) / 100f

        val groupsFromNumbers: Set<Int> = _personalNumbers
            .filter { it.isDigit() }
            .map { it.digitToInt() % 5 }
            .toSet()

        var favoredIndex = -1
        var favoredMod = -1
        if (text.isNotEmpty()) {
            val h = text.hashCode()
            favoredIndex = kotlin.math.abs(h) % pool.size
            favoredMod = kotlin.math.abs(h / 7) % 5
        }

        fun suitIndex(c: TarotCard): Int = when {
            c.index < 22 -> 0
            c.name.contains("Bastoni", true) -> 1
            c.name.contains("Coppe", true) -> 2
            c.name.contains("Spade", true) -> 3
            else -> 4
        }

        for (i in pool.indices) {
            val card = pool[i]
            var wi = 1f

            if (groupsFromNumbers.contains(suitIndex(card))) wi += 1f
            if (i == favoredIndex && favoredIndex >= 0) wi += 1f
            if (favoredMod >= 0 && suitIndex(card) == favoredMod) wi += 0.5f

            wi += MoodEngine.extraWeightForCard(card, boosts)
            w[i] = (1f * (1f - alpha)) + (wi * alpha)
        }

        for (i in w.indices) if (w[i] < 0.05f) w[i] = 0.05f
        val sum = w.sum().coerceAtLeast(1e-6f)
        for (i in w.indices) w[i] /= sum
        return w
    }

    private fun <T> weightedSampleWithoutReplacement(
        pool: List<T>, weights: FloatArray, k: Int, rnd: kotlin.random.Random,
    ): List<T> {
        if (pool.isEmpty()) return emptyList()
        val items = pool.toMutableList()
        val w = weights.toMutableList()
        val selected = mutableListOf<T>()
        repeat(k.coerceAtMost(items.size)) {
            val total = w.sum().coerceAtLeast(1e-6f)
            var r = rnd.nextFloat() * total
            var pick = -1
            for (i in w.indices) {
                r -= w[i]; if (r <= 0f) {
                    pick = i; break
                }
            }
            if (pick == -1) pick = w.lastIndex
            selected += items[pick]
            items.removeAt(pick)
            w.removeAt(pick)
        }
        return selected
    }

    private fun drawInternal(ctx: Context?) {
        val pool = buildList {
            if (_includeMajors) addAll(TarotDeck.cards.take(22))
            if (_includeMinors) addAll(TarotDeck.cards.drop(22))
        }
        if (pool.isEmpty()) return

        val rnd = rng()
        val weights = weightsFor(pool)
        val take = weightedSampleWithoutReplacement(pool, weights, _count, rnd)

        _drawn = take.map { c -> DrawnCard(c, _allowReversed && rnd.nextBoolean()) }.toMutableList()
        state["drawn"] = ArrayList(_drawn)

        ctx?.let { viewModelScope.launch { runCatching { Prefs.save(it, _drawn, _count) } } }
    }

    private fun resetInternal(ctx: Context?) {
        _drawn = mutableListOf()
        state["drawn"] = ArrayList<DrawnCard>()
        ctx?.let { viewModelScope.launch { runCatching { Prefs.clear(it) } } }
    }

    fun draw(ctx: Context?) = drawInternal(ctx)
    fun reset(ctx: Context?) = resetInternal(ctx)
    fun draw() = drawInternal(null)
    fun reset() = resetInternal(null)

    // ===== Vision → bias Maggiori suggeriti =====
    suspend fun applyVisionBias(provider: AiProvider, openKey: String, gemKey: String) {
        val v = _visionText.trim()
        if (v.isEmpty()) return

        when (provider) {
            AiProvider.OPENAI -> if (openKey.isNotBlank())
                LlmClient.visionToMajorsOpenAI(openKey, v) else LlmResult()

            AiProvider.GEMINI -> if (gemKey.isNotBlank())
                LlmClient.visionToMajorsGemini(gemKey, v) else LlmResult()

            else -> LlmResult()
        }

        if (MoodEngine.suggestedMajors.isNotEmpty()) {
            _llmBoosts = LlmBoosts(
                majors = 1.5f, wands = 1f, cups = 1f, swords = 1f, pentacles = 1f,
                specificMajors = MoodEngine.suggestedMajors.toSet(),
                specificMajorsBoost = 1.5f
            )
        }
    }
}
// ======================== END TAROT VIEWMODEL ========================

//=========================== ACTIVITY ===========================
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrapContext(ctx = newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Applica la lingua salvata PRIMA di creare la UI
        val savedLang = runBlocking { ApiConfig.langFlow(ctx = this@MainActivity).first() }
        val tag = savedLang.ifBlank { "it" }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))

        //inizializza gli ADS QUI
        //MobileAds.initialize(this)
        val vm by viewModels<TarotViewModel>()
        setContent { AppTheme { TarotScreen(vm = vm) } }
    }
}
//======================= END ACTIVITY ===========================

// =============================== ROOT SCREEN / UI ===============================
@Composable
fun TarotScreen(vm: TarotViewModel) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) { vm.loadFromStorage(ctx) }

    val (_, _, _, lang) = rememberAiPrefs()
    val deck by ApiConfig.deckFlow(ctx).collectAsState(initial = "base")
    TarotScreenContent(
        // stato
        llmBoosts = vm.llmBoosts,
        count = vm.count,
        includeMajors = vm.includeMajors,
        includeMinors = vm.includeMinors,
        allowReversed = vm.allowReversed,
        personalNumbers = vm.personalNumbers,
        mentalImageText = vm.mentalImageText,
        visionText = vm.visionText,
        influencePct = vm.influencePct,
        deterministic = vm.deterministic,
        drawn = vm.drawn,
        cardDisplayMode = vm.cardDisplayMode,
        deck = deck,
        showCreditsDialog = vm.showCreditsDialog,
        noSynonymFound = vm.noSynonymFound,
        moodAnalysisDone = vm.moodAnalysisDone,
        // callback
        onCountChange = vm::onCountChange,
        onToggleMajors = vm::onToggleMajors,
        onToggleMinors = vm::onToggleMinors,
        onToggleReversed = vm::onToggleReversed,
        onPersonalNumbersChange = vm::onPersonalNumbersChange,
        onMentalImageChange = vm::onMentalImageChange,
        onVisionChange = vm::onVisionChange, // aggiorna anche suggested in VM
        onInfluencePctChange = vm::onInfluencePctChange,
        onToggleDeterministic = vm::onToggleDeterministic,
        onDraw = vm::draw,
        onReset = vm::reset,
        onToggleCardDisplay = vm::onToggleCardDisplay,
        onSetShowCreditsDialog = vm::onShowCreditsDialog,
        onAnalyze = { vm.refreshLlmBoosts(ctx, lang) },
        onApplyVision = { p, ok, gk -> scope.launch { vm.applyVisionBias(p, ok, gk) } }
    )
}
// ============================= END ROOT SCREEN =============================

// =============== REMEMBER AI PREFS (DataStore) ===============
@Composable
fun rememberAiPrefs(): Quadruple<AiProvider, String, String, String> {
    val ctx = LocalContext.current
    val provider by ApiConfig.providerFlow(ctx).collectAsState(initial = AiProvider.NONE)
    val openKey by ApiConfig.openAiKeyFlow(ctx).collectAsState(initial = "")
    val gemKey by ApiConfig.geminiKeyFlow(ctx).collectAsState(initial = "")
    val lang by ApiConfig.langFlow(ctx).collectAsState(initial = "it")
    return Quadruple(provider, openKey, gemKey, lang)
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/* ==================== UI (schermo principale) ==================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreenContent(
    // Stato
    llmBoosts: LlmBoosts?,
    count: Int,
    includeMajors: Boolean,
    includeMinors: Boolean,
    allowReversed: Boolean,
    personalNumbers: String,
    mentalImageText: String,
    visionText: String,
    influencePct: Int,
    deterministic: Boolean,
    drawn: List<DrawnCard>,
    cardDisplayMode: CardDisplayMode,
    deck: String,
    showCreditsDialog: Boolean,
    // Nuovi flag per la logica “nessun sinonimo”
    noSynonymFound: Boolean,
    moodAnalysisDone: Boolean,
    // Callback
    onCountChange: (Int) -> Unit,
    onToggleMajors: (Boolean) -> Unit,
    onToggleMinors: (Boolean) -> Unit,
    onToggleReversed: (Boolean) -> Unit,
    onPersonalNumbersChange: (String) -> Unit,
    onMentalImageChange: (String) -> Unit,
    onVisionChange: (String) -> Unit,
    onInfluencePctChange: (Int) -> Unit,
    onToggleDeterministic: (Boolean) -> Unit,
    onDraw: () -> Unit,
    onReset: () -> Unit,
    onToggleCardDisplay: () -> Unit,
    onSetShowCreditsDialog: (Boolean) -> Unit,
    onAnalyze: () -> Unit,
    onApplyVision: (AiProvider, String, String) -> Unit,
    viewModel: TarotViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val ctx = LocalContext.current

    // Limiti versione free (10 giorni / 30 stese)
    val isLimited = !ExperienceGate.isPro && ExperienceGate.checkLimits(ctx)
    // --- Stato per la card di introduzione audio ---
    val prefs = ctx.getSharedPreferences("experience_prefs", Context.MODE_PRIVATE)

    var audioIntroCount by remember {
        mutableStateOf(prefs.getInt("audio_intro_count", 0))
    }

// Mostra la card automaticamente solo se è apparsa meno di 3 volte
    val showAudioIntro = viewModel.showAudioCard
    val scope = rememberCoroutineScope()
    val (provider, openKey, gemKey, lang) = rememberAiPrefs()

    /* ---------- Stato UI locale ---------- */
    var menuExpanded by remember { mutableStateOf(false) }
    var devSnack by remember { mutableStateOf<String?>(null) }
    var showDeckDialog by remember { mutableStateOf(false) }
    var showUpgradeDialog by rememberSaveable { mutableStateOf(false) }
    var showAiSettings by remember { mutableStateOf(false) }
    var showHelpAi by remember { mutableStateOf(false) }
    var showTarotGuide by remember { mutableStateOf(false) }
    var showLang by remember { mutableStateOf(false) }


    // Stato per lo spinner “online lookup” del campo Stato d’animo
    var isCanonicalizing by remember { mutableStateOf(false) }

    /* ---------- Gate esperienza: versione free / PRO ---------- */
    LaunchedEffect(Unit) {
        ExperienceGate.checkAndPrompt(
            context = ctx,
            onLimitReached = { showUpgradeDialog = true }
        )
    }

    /* ---------- AUDIO AMBIENTE (globale) ---------- */
    // Stato locale: musica ON/OFF (parte ON di default)
    var isMusicOn by rememberSaveable { mutableStateOf(true) }

    // Tieni allineato il flag globale di AmbientAudio
    LaunchedEffect(isMusicOn) {
        AmbientAudio.setEnabled(enabled = isMusicOn, ctx)
    }

    // Ogni volta che cambia mazzo o stato audio, aggiorna la musica
    LaunchedEffect(isMusicOn, deck) {
        if (!isMusicOn) return@LaunchedEffect

        val resId = when (deck.lowercase()) {
            "lux"          -> R.raw.amb_lux
            "cel"          -> R.raw.amb_cel
            "arc", "nova"  -> R.raw.amb_arc
            else           -> R.raw.amb_base   // default
        }

        AmbientAudio.play(
            ctx   = ctx,
            resId = resId,
            loop  = true
        )
    }

    /* ---------- Credits ---------- */
    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { onSetShowCreditsDialog(false) },
            title = { Text(stringResource(R.string.menu_credits)) },
            text = {
                Text(
                    "Programmato da Antonio Chimienti ©2025.\n" +
                            "Suggerimenti: info@antoniochimienti.it"
                )
            },
            confirmButton = {
                TextButton(onClick = { onSetShowCreditsDialog(false) }) {
                    Text("OK")
                }
            }
        )
    }

    /* ---------- Scenografia ---------- */
    val gradientColor = Color(0xFF403830)
    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.7f),
        offset = Offset(4f, 4f),
        blurRadius = 8f
    )
    val textStyleWithShadow = TextStyle(shadow = textShadow)

    /*========ANIMAZIONE LUCE========================*/

// Layer scuro (se poi vuoi, lo possiamo animare)
    val overlayAlpha = remember { Animatable(0.12f) }

// --- NUOVA LOGICA FLASH: subito + random 5–10s ---
    val flashProgress = remember { Animatable(1f) }
    var isFlashing by remember { mutableStateOf(false) }
    var firstFlashDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            if (!firstFlashDone) {
                // primo flash immediato
                firstFlashDone = true
            } else {
                // attesa casuale tra 5 e 10 secondi
                val delayMs = (25000..100000).random()
                delay(delayMs.toLong())
            }

            isFlashing = true
            flashProgress.snapTo(0f)

            flashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 450,      // durata del flash
                    easing = LinearEasing
                )
            )

            isFlashing = false
            flashProgress.snapTo(1f)
        }
    }
    /*=====================FINE ANIMAZIONE LUCE======================*/

    Box {
        // 1) Immagine di sfondo
        Image(
            painter = painterResource(id = R.drawable.sfondo_margine),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2) Velo a gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            gradientColor.copy(alpha = 0.9f),
                            Color.Transparent,
                            gradientColor.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // 3) Layer scuro + passata di luce tipo riflesso
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Sfondo scurito
                    drawRect(Color.Black.copy(alpha = overlayAlpha.value))

                    // Pivot in basso a destra
                    val pivot = Offset(size.width, size.height)

                    // Progresso dell’animazione (0 → 1)
                    val p = flashProgress.value

                    // Angolo: da -45° (taglia da sotto) a +45° (taglia da sopra)
                    val angleDeg = -45f + 180f * p

                    // Lama lunga: attraversa TUTTO il device
                    val beamLength = size.maxDimension * 1.2f

                    // Spessore della lama (abbastanza evidente)
                    val beamThickness = size.minDimension / 0.5f

                    // Intensità tipo "onda" (forte al centro, zero agli estremi)
                    val intensity = sin(p * PI).toFloat().coerceAtLeast(0f)

                    // Se è davvero bassissima, non disegno (così non sporca lo sfondo)
                    if (intensity < 0.05f) return@drawBehind

                    // Alpha modulati dall’intensità
                    val coreAlpha = 0.12f + 0.18f * intensity       // alone morbido
                    val highlightAlpha = 1.20f + 2.42f * intensity  // colpo centrale

                    // --- COLORI CELESTE MADONNA (più morbidi e meno flashati) ---
                    val blueCore = Color(0xFF1F1FFF)   // azzurro Madonna, profondo e tenue
                   val blueHi   = Color(0xFFCDE8F5)   // highlight morbido, non bianco freddo

                    // Lama di luce (gradient lineare) AZZURRA
                    val beamBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            blueCore.copy(alpha = 0f),
                            blueCore.copy(alpha = coreAlpha),
                            blueHi.copy(alpha = highlightAlpha),
                            blueCore.copy(alpha = coreAlpha),
                            blueCore.copy(alpha = 0f),
                            Color.Transparent
                        ),
                        start = pivot,
                        end = pivot - Offset(beamLength, 0f)
                    )

                    // Flare morbido sul raggio (azzurrino anche lui)
                    val flareRadius = beamThickness * 3.2f
                    val flareCenter = Offset(
                        x = size.width - beamLength * 0.45f,
                        y = size.height
                    )
                    val flareBrush = Brush.radialGradient(
                        colors = listOf(
                            blueHi.copy(alpha = 0.7f * intensity),
                            Color.Transparent
                        ),
                        center = flareCenter,
                        radius = flareRadius
                    )

                    withTransform({
                        rotate(
                            degrees = angleDeg,
                            pivot = pivot
                        )
                    }) {
                        // Lama che attraversa lo schermo
                        drawRect(
                            brush = beamBrush,
                            topLeft = Offset(
                                x = size.width - beamLength,
                                y = size.height - beamThickness / 2f
                            ),
                            size = Size(
                                width = beamLength,
                                height = beamThickness
                            ),
                            blendMode = BlendMode.ColorDodge   // ⭐ "Linear Dodge (Add)"
                        // blendMode = BlendMode.Plus   // ⭐ "Linear Dodge (Add)"
                        )

                        // Flare centrale
                        drawCircle(
                            brush = flareBrush,
                            radius = flareRadius,
                            center = flareCenter
                        )
                    }
                }        )

    /* ---------- Scaffold ---------- */
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.icona_marchio_1),
                                contentDescription = "Sanar Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                stringResource(R.string.app_name),
                                color = Color.White,
                                style = textStyleWithShadow
                            )
                        }
                    },
                    actions = {
                        //Menu verticale
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (cardDisplayMode == CardDisplayMode.COMPACT)
                                                stringResource(R.string.menu_toggle_view_images)
                                            else stringResource(R.string.menu_toggle_view)
                                        )
                                    },
                                    onClick = { onToggleCardDisplay(); menuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.menu_choose_deck)) },
                                    onClick = { menuExpanded = false; showDeckDialog = true }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_ai_settings)) },
                                    onClick = { showAiSettings = true; menuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_help_ai)) },
                                    onClick = { showHelpAi = true; menuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_tarot_guide)) },
                                    onClick = { showTarotGuide = true; menuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_language)) },
                                    onClick = { showLang = true; menuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_credits)) },
                                    onClick = { onSetShowCreditsDialog(true); menuExpanded = false }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_restore_audio_card)) },
                                    onClick = {
                                        viewModel.resetAudioCard()
                                        audioIntroCount = 0
                                        prefs.edit().putInt("audio_intro_count", 0).apply()
                                        menuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(if (isMusicOn) "Audio ON" else "Audio OFF")
                                    },
                                    onClick = {
                                        isMusicOn = !isMusicOn
                                        menuExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_check_images)) },
                                    onClick = {
                                        menuExpanded = false
                                        val missing = TarotDeck.cards.filter { card ->
                                            val resId = cardDrawableResForDeck(ctx, card, deck)
                                            resId == R.drawable.carta_nera
                                        }
                                        devSnack = if (missing.isEmpty())
                                            "✅ Tutte le immagini sono presenti"
                                        else
                                            "⚠️ Mancano ${missing.size} carte: " +
                                                    missing.joinToString(",") { it.name }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent,
            bottomBar = {
                if (drawn.isNotEmpty()) {
                    val appName = ctx.getString(R.string.app_name)
                    val fieldMood = ctx.getString(R.string.field_mood)
                    val fieldVision = ctx.getString(R.string.field_vision)
                    val shareLabel = ctx.getString(R.string.btn_share)

                    BottomAppBar(
                        containerColor = Color(0x80403830),
                        contentColor = Color.White
                    ) {
                        Spacer(Modifier.weight(1f))

                        // Bottone CONDIVIDI normale (sempre disponibile)
                        Button(
                            onClick = {
                                val shareText = buildString {
                                    append(appName).append("\n\n")
                                    drawn.forEachIndexed { i, d ->
                                        append("${i + 1}. ${d.card.name}")
                                        if (d.reversed) append(" (Rovesciata)")
                                        append("\n")
                                    }
                                    if (mentalImageText.isNotBlank())
                                        append("\n$fieldMood: $mentalImageText")
                                    if (visionText.isNotBlank())
                                        append("\n$fieldVision: $visionText")
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                ctx.startActivity(Intent.createChooser(intent, shareLabel))
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .height(48.dp)
                        ) {
                            Text(shareLabel)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            // 🔒 Bottone "Copia & apri ChatGPT/Gemini" SOLO se NON limitato
                            if (!isLimited) {
                                TextButton(
                                    onClick = {
                                        val shareText = buildString {
                                            append(appName).append("\n\n")
                                            drawn.forEachIndexed { i, d ->
                                                append("${i + 1}. ${d.card.name}")
                                                if (d.reversed) append(" (Rovesciata)")
                                                append("\n")
                                            }
                                            if (mentalImageText.isNotBlank())
                                                append("\n$fieldMood: $mentalImageText")
                                            if (visionText.isNotBlank())
                                                append("\n$fieldVision: $visionText")
                                        }

                                        val clip = android.content.ClipData.newPlainText(
                                            "Tarot Reading",
                                            shareText
                                        )
                                        (ctx.getSystemService(Context.CLIPBOARD_SERVICE)
                                                as android.content.ClipboardManager)
                                            .setPrimaryClip(clip)

                                        val candidates = listOf(
                                            "com.openai.chatgpt",
                                            "com.google.android.apps.bard"
                                        )
                                        var opened = false
                                        for (pkg in candidates) {
                                            val li = ctx.packageManager
                                                .getLaunchIntentForPackage(pkg)
                                            if (li != null) {
                                                li.addFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                                )
                                                runCatching {
                                                    ctx.startActivity(li)
                                                    opened = true
                                                }
                                                if (opened) break
                                            }
                                        }
                                        if (!opened) {
                                            val web = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://chat.openai.com/")
                                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            runCatching { ctx.startActivity(web) }
                                        }
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    val shareGpt =
                                        if (provider == AiProvider.GEMINI) {
                                            if (lang.startsWith("en", true))
                                                "Copy & open Gemini"
                                            else
                                                "Copia & apri Gemini"
                                        } else {
                                            if (lang.startsWith("en", true))
                                                "Copy & open ChatGPT"
                                            else
                                                "Copia & apri ChatGPT"
                                        }
                                    Text(shareGpt, color = Color.White)
                                }
                            }

                            val providerLabel = when (provider) {
                                AiProvider.OPENAI ->
                                    if (lang.startsWith("en", true))
                                        "Active provider: ChatGPT"
                                    else
                                        "Provider attivo: ChatGPT"

                                AiProvider.GEMINI ->
                                    if (lang.startsWith("en", true))
                                        "Active provider: Gemini"
                                    else
                                        "Provider attivo: Gemini"

                                else ->
                                    if (lang.startsWith("en", true))
                                        "Active provider: None"
                                    else
                                        "Provider attivo: Nessuno"
                            }

                            Text(
                                providerLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        ) { innerPadding ->

            /* ---------- QUICK HELP ---------- */
            var quickHelpOpen by rememberSaveable { mutableStateOf(false) }

            // --- Preferenze per non mostrare la card audio troppe volte ---
            val prefs = remember {
                ctx.getSharedPreferences("experience_prefs", Context.MODE_PRIVATE)
            }

            var audioIntroCount by remember {
                mutableStateOf(prefs.getInt("audio_intro_count", 0))
            }


// aggiunta paramentri per lo sfondo
// 🔹 Quando chiudi la card audio, scurisco gradualmente lo sfondo
            LaunchedEffect(showAudioIntro) {
                if (!showAudioIntro) {
                    overlayAlpha.animateTo(
                        targetValue = 0.65f,        // quanto scuro vuoi
                        animationSpec = tween(3000) // 3 secondi
                    )
                } else {
                    // se per qualche motivo la ri-mostriamo, torno chiaro
                    overlayAlpha.snapTo(0.12f)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- AUDIO INTRO CARD (mostrata solo se 'showAudioIntro' è true) ---
                if (showAudioIntro) {
                    AudioIntroCard(
                        langTag = currentLangTag(),
                        onDismiss = {
                            // 🔹 nascondo la card agendo sul ViewModel
                            viewModel.hideAudioCard()

                            // 🔹 aggiorno il contatore come facevi prima
                            audioIntroCount += 1
                            prefs.edit()
                                .putInt("audio_intro_count", audioIntroCount)
                                .apply()
                        }
                    )
                }

                /* ---------- TITOLO + QUICK HELP ---------- */
                Text(
                    text = stringResource(R.string.title_extract),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = textShadow
                    ),
                    color = Color.White
                )

                QuickHelpSectionV4(
                    langTag = currentLangTag(),
                    isPro = isProVersion,
                    open = quickHelpOpen,
                    onToggle = { quickHelpOpen = !quickHelpOpen },
                    onChooseCards = { },
                    onFocusMood = { },
                    onOpenMainMenu = { menuExpanded = true },
                    onOpenTarotGuide = { showTarotGuide = true }
                )

                // Numero di carte
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.field_num_cards),
                        color = Color.White,
                        style = TextStyle(shadow = textShadow)
                    )
                    CountStepper(
                        value = count,
                        onDecrement = { onCountChange((count - 1).coerceAtLeast(1)) },
                        onIncrement = { onCountChange((count + 1).coerceAtMost(15)) }
                    )
                }

// --- Switch base: Arcani Maggiori / Minori / Rovesciate ---
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = includeMajors, onCheckedChange = onToggleMajors)
                        Text(
                            stringResource(R.string.toggle_majors),
                            color = Color.White,
                            style = TextStyle(shadow = textShadow),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = includeMinors, onCheckedChange = onToggleMinors)
                        Text(
                            stringResource(R.string.toggle_minors),
                            color = Color.White,
                            style = TextStyle(shadow = textShadow),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = allowReversed, onCheckedChange = onToggleReversed)
                        Text(
                            stringResource(R.string.toggle_reversed),
                            color = Color.White,
                            style = TextStyle(shadow = textShadow),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }


                // === DIAGNOSTICA MOOD (una sola volta, sopra il campo Stato d’animo) ===
                MoodDiagnosticsCard(
                    mentalText = mentalImageText,
                    llmBoosts = llmBoosts
                )

                // === CAMPO "STATO D'ANIMO" con spinner + messaggio "nessun sinonimo" ===
                run {
                    val langIsEn = lang.startsWith("en", ignoreCase = true)

                    // Stati locali UI
                    var isCanonicalizing by remember { mutableStateOf(false) }
                    var showNoSynWarning by remember { mutableStateOf(false) }
                    var job by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

                    // Helper: verifica se il testo contiene almeno una parola “riconoscibile”
                    suspend fun hasKnownWordSafe(q: String): Boolean {
                        val useOpenAI = when (provider) {
                            AiProvider.OPENAI -> openKey.isNotBlank()
                            AiProvider.GEMINI -> gemKey.isNotBlank()
                            else -> false
                        }
                        return MoodEngine.hasAnyKnownWord(
                            text = q,
                            ctx = ctx,
                            langTag = lang,
                            useDatamuse = true,
                            useOpenAI = useOpenAI,
                            openAiKey = if (provider == AiProvider.OPENAI) openKey else ""
                        )
                    }

                    // ⚠️ Messaggio “nessun sinonimo” – SOPRA il campo
                    if (showNoSynWarning && mentalImageText.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 4.dp, start = 6.dp, end = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (langIsEn)
                                    "No synonym found (try a simpler word, e.g. \"sad\", \"joy\", \"angry\"…)."
                                else
                                    "Nessun sinonimo trovato (prova una parola più semplice, es. \"triste\", \"gioia\", \"rabbia\"…).",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Campo input STATO D'ANIMO
                    ThemedFieldLight(
                        value = mentalImageText,
                        onChange = { newText ->
                            onMentalImageChange(newText)
                            showNoSynWarning = false
                            job?.cancel()

                            val q = newText.trim()
                            if (q.length < 3) {
                                isCanonicalizing = false
                                return@ThemedFieldLight
                            }

                            job = scope.launch {
                                isCanonicalizing = true
                                delay(300)

                                // 1) Esegui SEMPRE l’analisi
                                onAnalyze()

                                // 2) Controlla se esiste almeno una parola “riconoscibile”
                                val known = try {
                                    hasKnownWordSafe(q)
                                } catch (_: Exception) {
                                    true
                                }
                                showNoSynWarning = !known

                                // 3) chiudi spinner
                                delay(100)
                                isCanonicalizing = false
                            }
                        },
                        label = stringResource(R.string.field_mood),
                        placeholder = stringResource(R.string.placeholder_mood),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rotellina bianca durante lookup
                    if (isCanonicalizing) {
                        Row(
                            Modifier.padding(top = 6.dp, start = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (langIsEn) "Looking up synonyms…" else "Cerco sinonimi…",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // ---------- Campo “Visione” ----------
                ThemedFieldLight(
                    value = visionText,
                    onChange = onVisionChange,
                    label = stringResource(R.string.field_vision),
                    placeholder = stringResource(R.string.placeholder_vision),
                    modifier = Modifier.fillMaxWidth()
                )

                LaunchedEffect(visionText, lang) {
                    MoodEngine.updateSuggestedMajorsFromText(
                        visionText,
                        lang
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    val drawLabel = ctx.getString(R.string.btn_draw)
                    val resetLabel = ctx.getString(R.string.btn_reset)

                    Button(
                        onClick = {
                            // 1) Limiti FREE (come prima)
                            if (!ExperienceGate.isPro) {
                                ExperienceGate.registerDraw(ctx)
                                if (ExperienceGate.checkLimits(ctx)) {
                                    showUpgradeDialog = true
                                }
                            }

                            // 2) Effetto fade in / fade out sullo sfondo (2s + 2s)
                            scope.launch {
                                val baseAlpha = overlayAlpha.value
                                val flashAlpha = (baseAlpha + 0.25f).coerceAtMost(0.75f)

                                // fade in (più scuro / più intenso)
                                overlayAlpha.animateTo(
                                    targetValue = flashAlpha,
                                    animationSpec = tween(durationMillis = 2000)
                                )

                                // fade out, torno al valore di base
                                overlayAlpha.animateTo(
                                    targetValue = baseAlpha,
                                    animationSpec = tween(durationMillis = 2000)
                                )
                            }

                            // 3) Logica originale: applyVision + draw
                            if (visionText.isNotBlank() && provider != AiProvider.NONE) {
                                scope.launch {
                                    onApplyVision(provider, openKey, gemKey)
                                    onDraw()
                                }
                            } else {
                                onDraw()
                            }
                        },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(drawLabel)
                    }

                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(resetLabel)
                    }
                }

                /* ---------- Carte estratte ---------- */
                if (drawn.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    if (cardDisplayMode == CardDisplayMode.FULL_IMAGE) {
                        Text(stringResource(R.string.section_drawn_images), color = Color.White)
                        val config = LocalConfiguration.current
                        val isLandscape =
                            config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                        val minSize = if (isLandscape) 140.dp else 120.dp
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 1200.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            items(drawn) { drawnCard ->
                                val resCandidate = remember("${deck}:${drawnCard.card.index}") {
                                    cardDrawableResForDeck(ctx, drawnCard.card, deck)
                                }
                                val safeRes =
                                    if (resCandidate != 0) resCandidate else R.drawable.carta_nera
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.66f)
                                        .graphicsLayer {
                                            if (drawnCard.reversed) rotationZ = 180f
                                        }
                                ) {
                                    Image(
                                        painter = painterResource(id = safeRes),
                                        contentDescription = drawnCard.card.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.section_drawn_list),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(shadow = textShadow)
                        )
                        drawn.forEach { d ->
                            Text(
                                text = "• ${d.card.name} ${if (d.reversed) "(Rovesciata)" else ""}",
                                color = Color.White,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(72.dp))
            }
        } // ======== FINE SCAFFOLD ========

        /* ---------- Snackbar ---------- */
        devSnack?.let { msg ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    action = { TextButton(onClick = { devSnack = null }) { Text("OK") } }
                ) { Text(msg) }
            }
        }
    }

    /* ---------- Dialog impostazioni AI ---------- */
    if (showAiSettings) AiSettingsDialog(onClose = { showAiSettings = false })

    /* ---------- Dialog guida AI ---------- */
    if (showHelpAi) {
        AssetDialog(
            title = stringResource(R.string.menu_help_ai),
            assetPath = assetForCurrent("help_ai"),
            onClose = { showHelpAi = false }
        )
    }

    /* ---------- Dialog guida Tarocchi ---------- */
    if (showTarotGuide) {
        AssetDialog(
            title = stringResource(R.string.menu_tarot_guide),
            assetPath = assetForCurrent("tarot_guide"),
            onClose = { showTarotGuide = false }
        )
    }

    /* ---------- Dialog lingua ---------- */
    if (showLang) {
        val langScope = rememberCoroutineScope()
        val activity = LocalContext.current as? Activity
        AlertDialog(
            onDismissRequest = { showLang = false },
            confirmButton = {},
            title = { Text(text = stringResource(id = R.string.menu_language)) },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fun applyLanguage(tag: String) {
                        langScope.launch { ApiConfig.setLang(ctx, tag) }
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        LocaleManager.persist(ctx = ctx, lang = tag)
                        showLang = false
                        activity?.recreate()
                    }
                    Button(onClick = { applyLanguage("it") }) { Text("Italiano") }
                    Button(onClick = { applyLanguage("en") }) { Text("English") }
                }
            }
        )
    }

    /* ---------- Dialog scelta mazzo ---------- */
    if (showDeckDialog) {
        DeckPickerDialog(
            current = deck,
            onSelect = { newDeck ->
                scope.launch {
                    ApiConfig.setDeck(ctx, newDeck)
                    showDeckDialog = false
                }
            },
            onDismiss = { showDeckDialog = false }
        )
    }
}    /* ====================== QUICK HELP (chip + pannello) ====================== */
// 1) Seleziona numero carte
// 2) (Opzionale) Descrivi stato d'animo
// 3) Premi "Estrai carte"
// 4) Impara più dettagli (tre puntini)
// Nessun pulsante “Estrai” dentro il chip


@Composable
fun QuickHelpSectionV4(
    langTag: String,
    isPro: Boolean,
    open: Boolean,
    onToggle: () -> Unit,
    onChooseCards: () -> Unit,
    onFocusMood: () -> Unit,
    onOpenMainMenu: () -> Unit,
    onOpenTarotGuide: () -> Unit,    // <- NEW: apre la guida "Cosa sono i tarocchi?"
) {
    val isEn = langTag.startsWith("en", ignoreCase = true)

    // ── Chips in riga: "Come funziona?" + "Cosa sono i tarocchi?"
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Chip 1: “Come funziona?”
        AssistChip(
            onClick = onToggle,
            label = { Text(if (isEn) "How it works?" else "Come funziona?") },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = Color(0x33FFFFFF),
                labelColor = Color.White
            )
        )

        // 💫 Chip 2: “Cosa sono i Tarocchi?” — vetroso magenta-rosato con icona dorata
        val chipShape = RoundedCornerShape(18.dp)
        val chipHeight = 40.dp
        val iconGold = Color(0xFFFFE082) // giallo caldo

        AssistChip(
            onClick = onOpenTarotGuide,
            label = {
                Text(
                    text = if (isEn) "What are Tarot cards?" else "Cosa sono i Tarocchi?",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = null,
                    tint = iconGold
                )
            },
            shape = chipShape,
            modifier = Modifier
                .height(chipHeight)
                .clip(chipShape),
            colors = AssistChipDefaults.assistChipColors(
                // vetroso magenta-rosato scuro: visibile ma elegante
                containerColor = Color(0x40B0005F), // traslucido, tono “vino-rosa”
                labelColor = Color.White,
                leadingIconContentColor = iconGold
            )
        )
    }
    if (!open) return

    Spacer(Modifier.height(8.dp))

    // Pannello espandibile (4 punti)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (isEn) "Quick start" else "Guida rapida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 1) Numero carte
            ClickRow(
                index = 1,
                text = if (isEn) "Choose how many cards" else "Seleziona il numero di carte",
                onClick = onChooseCards
            )

            // 2) Mood
            ClickRow(
                index = 2,
                text = if (isEn) "Write your Mood (optional)" else "Scrivi lo stato d’animo (facoltativo)",
                onClick = onFocusMood
            )

            // 3) Estrai (solo testo informativo)
            InfoRow(
                index = 3,
                text = if (isEn) "Press the button below: Draw cards" else "Premi il pulsante in basso: Estrai carte"
            )

            // 4) Tre puntini
            ClickRow(
                index = 4,
                text = if (isEn) "Learn more details (⋮ menu)" else "Impara più dettagli (menu ⋮)",
                onClick = onOpenMainMenu,
                underline = true
            )
        }
    }
}

@Composable
private fun ClickRow(
    index: Int,
    text: String,
    onClick: () -> Unit,
    underline: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$index)", fontWeight = FontWeight.Bold, color = Color.White)
        TextButton(onClick = onClick) {
            Text(
                text,
                color = Color.White,
                textDecoration = if (underline) TextDecoration.Underline else TextDecoration.None
            )
        }
    }
}

@Composable
private fun InfoRow(index: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$index)", fontWeight = FontWeight.Bold, color = Color.White)
        Text(text, color = Color.White)
    }
}

/* ==================== WIDGETS VARI ==================== */

@Composable
fun CountStepper(value: Int, onDecrement: () -> Unit, onIncrement: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onDecrement, enabled = value > 1) { Text("-") }
        Text("$value", style = MaterialTheme.typography.titleMedium, color = Color.White)
        Button(onClick = onIncrement, enabled = value < 15) { Text("+") }
    }
}

@Composable
fun ThemedFieldLight(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val shadow = Shadow(
        color = Color.Black.copy(alpha = 0.75f),
        offset = Offset(2f, 2f),
        blurRadius = 6f
    )
    val labelStyle =
        MaterialTheme.typography.labelLarge.copy(color = Color.White, shadow = shadow)
    val textStyle =
        MaterialTheme.typography.bodyLarge.copy(color = Color.White, shadow = shadow)

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, style = labelStyle) },
        placeholder = {
            Text(
                placeholder,
                color = Color.White.copy(alpha = 0.55f),
                style = textStyle
            )
        },
        textStyle = textStyle,
        singleLine = false,
        maxLines = 6,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.85f),
            focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.7f),
            cursorColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    )
}


/* ==================== DIAGNOSTICA MOOD (colori per seme) ==================== */
@Composable
fun MoodDiagnosticsCard(mentalText: String, llmBoosts: LlmBoosts?) {

    // Palette per seme (coerente e leggibile su sfondo scuro)
    val COLOR_MAJORS = Color(0xFF7E57C2) // viola
    val COLOR_WANDS = Color(0xFFFFA726) // arancio
    val COLOR_CUPS = Color(0xFF29B6F6) // azzurro
    val COLOR_SWORDS = Color(0xFFEF5350) // rosso
    val COLOR_PENTACLES = Color(0xFF66BB6A) // verde
    val TRACK = Color.White.copy(alpha = 0.20f)

    // Row con barra colorata per seme
    @Composable
    fun ProgressRow(label: String, value: Float, barColor: Color) {
        val clamped = value.coerceIn(0f, 2f)
        val target = clamped / 2f // 0..1
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(450),
            label = "boostProgress"
        )
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                // Mostro offset da 1.0 (neutro)
                val delta = clamped - 1f
                val sign = if (delta > 0f) "+" else ""
                Text(
                    "$sign${"%.1f".format(delta)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
            LinearProgressIndicator(
                progress = { animated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = barColor,               // <-- colore per seme
                trackColor = TRACK
            )
        }
    }

    val local = MoodEngine.analyzeMoodLocally(mentalText.trim())
    val merged = MoodEngine.merge(local, llmBoosts)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF),
            contentColor = Color.White
        )
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.mood_active),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (mentalText.isBlank()) {
                Text(stringResource(R.string.mood_hint))
                return@Column
            }

            Text("“$mentalText”", style = MaterialTheme.typography.bodyMedium)

            // Barre per seme con colori dedicati
            ProgressRow(stringResource(R.string.toggle_majors), merged.majors, COLOR_MAJORS)
            ProgressRow("Bastoni", merged.wands, COLOR_WANDS)
            ProgressRow("Coppe", merged.cups, COLOR_CUPS)
            ProgressRow("Spade", merged.swords, COLOR_SWORDS)
            ProgressRow("Denari", merged.pentacles, COLOR_PENTACLES)

            val majorsSet = merged.specificMajors.sorted()
            if (majorsSet.isNotEmpty()) {
                Text(
                    "Arcani favoriti: ${
                        majorsSet.joinToString(", ") { TarotDeck.cards[it].name }
                    } (+${"%.1f".format(merged.specificMajorsBoost)})",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (llmBoosts != null) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            stringResource(R.string.ai_boosts_active),
                            color = Color.White
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                    )
                )
            }
        }
    }
}

/* ==================== DIALOG: Impostazioni AI ==================== */
@Composable
fun AiSettingsDialog(onClose: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val provider by ApiConfig.providerFlow(ctx).collectAsState(initial = AiProvider.NONE)
    val openKey by ApiConfig.openAiKeyFlow(ctx).collectAsState(initial = "")
    val gemKey by ApiConfig.geminiKeyFlow(ctx).collectAsState(initial = "")

    var tmpProv by remember(provider) { mutableStateOf(provider) }
    var tmpOpen by remember(openKey) { mutableStateOf(openKey) }
    var tmpGem by remember(gemKey) { mutableStateOf(gemKey) }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    ApiConfig.setProvider(ctx, tmpProv)
                    ApiConfig.setOpenAiKey(ctx, tmpOpen.trim())
                    ApiConfig.setGeminiKey(ctx, tmpGem.trim())
                }
                onClose()
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.cancel)) } },
        title = { Text(stringResource(R.string.menu_ai_settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.ai_provider))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = (tmpProv == AiProvider.OPENAI),
                        onClick = { tmpProv = AiProvider.OPENAI },
                        label = { Text("OpenAI") })
                    FilterChip(
                        selected = (tmpProv == AiProvider.GEMINI),
                        onClick = { tmpProv = AiProvider.GEMINI },
                        label = { Text("Gemini") })
                    FilterChip(
                        selected = (tmpProv == AiProvider.NONE),
                        onClick = { tmpProv = AiProvider.NONE },
                        label = { Text(stringResource(R.string.none)) })
                }
                OutlinedTextField(
                    value = tmpOpen,
                    onValueChange = { tmpOpen = it },
                    label = { Text(stringResource(R.string.openai_key)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = tmpGem,
                    onValueChange = { tmpGem = it },
                    label = { Text(stringResource(R.string.gemini_key)) },
                    singleLine = true
                )
                Text(
                    stringResource(R.string.ai_notes),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

/* =====================  MD ASSETS (IT/EN con fallback)  ===================== */

private fun normalizeMdPath(name: String): String {
    val withExt = if (name.endsWith(".md", true)) name else "$name.md"
    return if (withExt.startsWith("decks/")) withExt else "decks/$withExt"
}

private fun loadMarkdownWithFallback(ctx: Context, requestedPath: String): String {
    runCatching { ctx.assets.open(requestedPath).bufferedReader().use { return it.readText() } }
    val rx = Regex("""^(.*)_(it|en)\.md$""", RegexOption.IGNORE_CASE)
    val m = rx.matchEntire(requestedPath.removePrefix("decks/"))
    if (m != null) {
        val base = m.groupValues[1]
        val lang = m.groupValues[2].lowercase()
        val alt = "decks/${base}_" + if (lang == "en") "it" else "en" + ".md"
        runCatching { ctx.assets.open(alt).bufferedReader().use { return it.readText() } }
    }
    val list = runCatching { ctx.assets.list("decks")?.joinToString("\n") ?: "" }.getOrNull()
    return "⚠️ Unable to load: $requestedPath\n\nAssets in /decks:\n$list"
}

@Composable
fun AssetDialog(title: String, assetPath: String, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val resolved = normalizeMdPath(assetPath)
    val mdText by remember(resolved) { mutableStateOf(loadMarkdownWithFallback(ctx, resolved)) }
    val scroll = rememberScrollState()

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = { TextButton(onClick = onClose) { Text("OK") } },
        title = { Text(text = title) },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp, max = 560.dp)
                    .verticalScroll(scroll)
            ) { MarkdownFull(text = mdText) }
        }
    )
}

@Composable
fun DeckRadioRow(value: String, label: String, selected: String, onPick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = (selected == value), onClick = { onPick(value) })
        Spacer(Modifier.width(8.dp)); Text(text = label)
    }
}

   /* ==================== LINGUA CORRENTE + ASSET PATH ==================== */

// Lingua corrente dell’app
fun currentLangTag(): String {
    val locales = AppCompatDelegate.getApplicationLocales()
    return if (locales.isEmpty) {
        java.util.Locale.getDefault().toLanguageTag()
    } else {
        (0 until locales.size()).joinToString(",") { idx ->
            locales[idx]?.toLanguageTag() ?: ""
        }
    }
}

// Percorso asset in base alla lingua corrente.
fun assetForCurrent(baseName: String): String {
    val isEn = currentLangTag().startsWith("en", ignoreCase = true)
    // Adatta come preferisci: suffisso o sottocartella
    return "decks/${baseName}_" + if (isEn) "en" else "it"
}
@Composable
fun AudioIntroCard(
    langTag: String,
    onDismiss: () -> Unit
) {
    val isEn = langTag.startsWith("en", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xAA000000),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
        ) {

            // 🔹 LOGO TRASPARENTE, PICCOLO, CENTRATO IN BASSO
            Image(
                painter = painterResource(id = R.drawable.ic_mediterranean_logo_round),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)   // ⭐ centrato orizzontalmente
                    .padding(bottom = 7.dp)         // ⭐ distanza dal bordo
                    .size(60.dp)                    // ⭐ regolabile (90–120 ideale)
                    .alpha(0.42f),                   // ⭐ trasparenza elegante
                contentScale = ContentScale.Fit
            )

            // ------- Contenuto testuale -------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "Immersive ambient audio" else "Audio ambientale immersivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isEn) {
                        "We are Mediterranean Records, a music label, and we have taken special care of the sound experience of this app, making it evocative, immersive and cinematic."
                    } else {
                        "Noi siamo Mediterranean Records, un’etichetta discografica, e per questo abbiamo dedicato una cura speciale all’esperienza sonora di questa app, rendendola evocativa, immersiva e dal carattere cinematografico."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (isEn) {
                        "For an even deeper experience, we suggest using headphones and listening to the ambient soundtrack for 3–5 minutes before your first spread."
                    } else {
                        "Per un’esperienza ancora più profonda ti consigliamo di usare le cuffie e restare in ascolto del commento musicale per 3–5 minuti prima della prima stesa."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (isEn) {
                        "For example, the church ambience (Celestia deck) is designed to give an almost three-dimensional feeling, as if you were really sitting on a bench in that space."
                    } else {
                        "Ad esempio l’ambiente sonoro della chiesa (mazzo Celestia) è stato pensato per dare una sensazione quasi tridimensionale, come se fossi davvero seduto su una panca in quel luogo."
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Mediterranean Records – Publishing",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = if (isEn) "Ok, got it" else "Ok, ho capito",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}