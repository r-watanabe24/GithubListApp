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

##### ViewModel例
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
  - クラス、ファイル名はUpperPascalCase、その他はlowerCamelCaseにすること

### 禁止事項
- ViewがRepository/DataSourceを直接参照 
- ViewModelがDataSourceを直接参照
- ViewModelがMutableStateFlowをそのまま公開 
- RepositoryがUIロジック/画面状態を保持 
- 依存を直接インスタンス化する
- 依存方向の逆流や層飛び越え
- 命名規則やディレクトリ、ファイルの配置の無視

### その他
- 修正すべき場合は、修正例を示すこと
- 必ず日本語でレビューすること