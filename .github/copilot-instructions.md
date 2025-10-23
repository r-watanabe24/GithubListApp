# GitHub Copilot Instructions
レビュー内容は必ず日本語で記述してください
Please review in Japanese

## レビュー観点
### アーキテクチャ / 設計
- アーキテクチャ：MVVM + Repository 
  - 依存方向：View → ViewModel → Repository → DataSource(API/DB/Cache)
  - 逆流や層の飛び越えをしてはいけない

#### View 
  - UI描画とユーザーの入力のみを担当する 
  - イベントをViewModelにXxxInput として渡す 
  - ViewModelのOutput(uiState)を購読し、Viewを更新する

#### ViewModel
  - InputインターフェースにEvent、OutputインターフェースにuiStateを定義する
  - EventをもとにuiStateを更新する
  - Eventによっては、repositoryのメソッドを呼び、完了時にuiStateを更新する
  - データ取得は Repository 経由のみ

#### Repository
  - DataSourceを統合し、RepositoryからのみDataSourceを呼び出すこと
  - UIロジックや画面状態を保持してはいけない
  - XXXRepository クラスと XXXRepositoryProtocol プロトコルを作成すること

#### DataSource（API/DB/Cache）
  - 責務別に分離すること

#### DI
  - DIフレームワークHiltの使用を必須
  - インターフェースに依存させること
  - 直接インスタンス化をしてはいけない

### パッケージ構成
com.example.app/
├── screens/
│   └── <screen>/
│       ├── view/                   # Activity / Fragment / Adapter
│       ├── viewmodel/              # XxxViewModel + Input/Output
│       ├── repository/             # XxxRepository + XxxRepositoryImpl
│       └── model/
│
├── datasource/
│   ├── api/
│   ├── db/
│   └── cache/
│
├── di/                             # Hilt
└── util/                           # 拡張関数

### 命名規則
- 変数名、メソッド名
  - 命名は役割と意味がわかるようにすること
  - メソッド名は動詞系にし、コールバックは先頭に「on」「did」「will」などをつけること

### 実装例

#### View層の例
```kotlin
class XxxFragment : Fragment(R.layout.fragment_xxx) {

  private val vm: XxxViewModel by viewModels()
  private lateinit var recycler: RecyclerView
  private val adapter = XxxAdapter()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setupViews(view)
    bindInput(view)
    bindOutput(view)
  }

  private fun setupViews(root: View) {
    recycler = root.findViewById(R.id.recycler)
    recycler.layoutManager = LinearLayoutManager(requireContext())
    recycler.adapter = adapter
  }

  private fun bindInput(root: View) {
    val input = root.findViewById<EditText>(R.id.input)
    root.findViewById<Button>(R.id.button).setOnClickListener {
      vm.onEvent(XxxEvent.TapButton(input.text?.toString().orEmpty()))
    }
    adapter.setOnItemClickListener { ui ->
      vm.onEvent(XxxEvent.TapItem(ui.id))
    }
    vm.onEvent(XxxEvent.LoadInitial)
  }

  private fun bindOutput(root: View) {
    val progress = root.findViewById<ProgressBar>(R.id.progress)
    val owner = viewLifecycleOwner
    owner.lifecycleScope.launch {
      owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          vm.uiState
            .map { it.isLoading }
            .distinctUntilChanged()
            .collectLatest { progress.isVisible = it }
        }
        launch {
          vm.uiState
            .map { it.items }
            .distinctUntilChanged()
            .collectLatest { adapter.submitList(it) }
        }
      }
    }
  }
}
```

#### ViewModel層の例
```kotlin
sealed interface XxxEvent {
  data class TapButton(val text: String) : XxxEvent
}

data class XxxUiState(
  val isLoading: Boolean = false
)
interface XxxViewModelInput {
    fun onEvent(e: XxxEvent)
}
interface XxxViewModelOutput {
  val uiState: StateFlow<XxxUiState>
}

@HiltViewModel
class XxxViewModel @Inject constructor(
  private val repo: XxxRepositoryProtocol
) : ViewModel(), XxxViewModelInput, XxxViewModelOutput {
  private val _uiState = MutableStateFlow(XxxUiState())
  override val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

  override fun onEvent(event: XxxEvent) = when (event) {
      is XxxEvent.TapButton -> {
          _uiState.update { it.copy(isLoading = true) }
      }
  }
}
```

#### Repository層の例
```kotlin
interface XxxRepositoryProtocol {
  suspend fun fetchXxx(param: Int): List<XxxEntity>
}

class XxxRepositoryImpl @Inject constructor(
  private val remote: XxxRemoteDataSource,
) : XxxRepositoryProtocol {
  override suspend fun fetchXxx(param: Int): List<XxxEntity> {
    val dtoList = remote.fetchXxxList(param)
    // Domainモデルへ変換
    return dtoList.map { dto ->
      XxxEntity(
        id   = dto.id,
        name = dto.name.orEmpty(),
        // ...必要なマッピング...
      )
    }
  }
}
```

#### DataSource層の例
```kotlin
class XxxRemoteDataSource @Inject constructor(
    private val apiClient: APIClient,
    private val moshi: Moshi
) {
    suspend fun fetchXxxList(param: Int): List<XxxDto> {
        // APIClientでGETリクエストを実行
        val result: ApiResult<String> = apiClient.get(
            path = "xxx",
            query = mapOf("param" to param.toString())
        )
        return when(result) {
            is ApiResult.Success -> {
                // JSON文字列をDTOリストにパース
                moshi.adapter<List<XxxDto>>(
                    Types.newParameterizedType(List::class.java, XxxDto::class.java)
                ).fromJson(result.data).orEmpty()
            }
            is ApiResult.Error -> {
                // エラー時の処理
                emptyList()
            }
        }
    }
}
```

### 禁止事項
- ViewがRepository/DataSourceを直接参照
- ViewModelがDataSourceを直接参照
- ViewModelがMutableStateFlowをそのまま公開
- RepositoryがUIロジック/画面状態を保持
- 依存を直接インスタンス化する
- 依存方向の逆流や層飛び越え
- 命名規則やディレクトリ、ファイルの配置の無視

### その他
- 上記実装例と大きくかけ離れないようにすること。
  - 必要に応じて局所的に手法が異なるのは問題なし、とみなす
  - InputとOutputは明示的に分離すること
- 重複した処理、または今後共通して使用できる処理は共通化すること
- 修正すべき場合は、修正例を示すこと
  - 局所的な修正の提案がある場合はdiff形式で記し、そうでない場合はmarkDownのシンタックスハイライトで示すこと
  - markDownのシンタックスハイライトで修正例などのkotlinコードを記載する際は、「必ず」スコープごとにtabでインデントして見やすくすること
- 既存コードがMVVMではないことがあるので、既存コードのアーキテクチャと新規実装コードの「MVVM + Repository」は混在してもよい
- 必ず日本語でレビューすること