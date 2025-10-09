# GitHub Copilot Instructions
レビュー内容は必ず日本語で記述してください
Please review in Japanese

## レビュー観点
### アーキテクチャ / 設計
- アーキテクチャ：MVVM + Repository 
  - 依存方向：View → ViewModel → Repository → DataSource(API/DB/Cache)
  - 逆流や層の飛び越えをしてはいけない

- View 
  - UI描画とユーザーの入力のみを担当する 
  - イベントをViewModelにXxxInput として渡す 
  - ViewModelのOutput(uiState)を購読し、Viewを更新する

- ViewModel
  - InputインターフェースにEvent、OutputインターフェースにuiStateを定義する
  - EventをもとにuiStateを更新する
  - Eventによっては、repositoryのメソッドを呼び、完了時にuiStateを更新する
  - データ取得は Repository 経由のみ

- Repository
  - DataSourceを統合し、RepositoryからのみDataSourceを呼び出すこと
  - UIロジックや画面状態を保持してはいけない
  - XXXRepository クラスと XXXRepositoryProtocol プロトコルを作成すること

- DataSource（API/DB/Cache）
  - 責務別に分離すること

- DI
  - DIフレームワークHiltの使用を必須
  - インターフェースに依存させること
  - 直接インスタンス化をしてはいけない
