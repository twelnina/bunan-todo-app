<p align="right">
  <a href="./README.md">English</a> | 日本語
</p>

# Bunan Todo

**Bunan Todo**は、その名の通り「無難」をコンセプトにしたTodoアプリです。

日々のタスク管理に必要な機能を過不足なく備え、余計な機能や複雑さを加えないシンプルさを大切にしています。

予定日を軸に、今日やることを無理なく整理できるAndroid向けTodoアプリです。

## ダウンロード

最新の署名済みAPKは[GitHub Releases](https://github.com/twelnina/android-todo-app/releases/latest)からダウンロードできます。

1. 最新のReleaseから `app-release.apk` をダウンロードします。
2. Android端末でAPKを開き、画面の案内に従ってインストールします。
3. 確認画面が表示された場合は、ブラウザまたはファイル管理アプリに「不明なアプリのインストール」を許可します。

Bunan TodoはAndroid 10（API 29）以上に対応しています。

## Bunan Todoについて

Bunan Todoは、今日のTodoと予定日を過ぎた未完了のTodoをひと目で確認できるタスク管理アプリです。Todoを予定日やタグで整理し、ホーム・リスト・カレンダーの3つの表示を使い分けて、そのとき必要なタスクに集中できます。

データはすべて端末内に保存されるため、オフラインでも利用できます。

このプロジェクトは、モダンなAndroid開発を学ぶための実践的な参考資料としても活用できます。Jetpack Compose、ViewModel、StateFlow、Room、自動テストを、小さく取り組みやすいアプリの中でどのように組み合わせるかを確認できます。

## 機能

- **Todoの管理** — タイトル、説明、予定日、タグを設定し、Todoの追加・編集・削除・完了状態の切り替えができます。
- **今日のタスクをひと目で確認** — 今日のTodoと予定日を過ぎた未完了のTodoを分けて確認し、予定日を過ぎたTodoはホーム画面から直接予定日を変更できます。
- **検索と絞り込み** — タイトルと説明のキーワード検索、複数タグの選択、予定日による絞り込みができます。
- **予定日別のリスト** — Todoを日付ごとに確認し、「すべて」「今日」「明日」「今週」「予定日超過」「予定なし」をすばやく切り替えられます。
- **カレンダー表示** — 月ごとの各日に登録されたTodoの件数と、選択した日のTodoを確認できます。
- **5種類のタグ** — 勉強、仕事、健康、趣味、買い物のタグでTodoを整理できます。
- **削除の取り消し** — Todoを削除した直後に、スナックバーから元に戻せます。
- **日本語・英語対応** — 端末の言語設定またはアプリごとの言語設定に合わせて表示します。
- **テーマ対応** — ライト・ダークテーマと、Android 12以降のダイナミックカラーに対応しています。

## 使用技術

| 分類 | 技術 |
| --- | --- |
| 言語 | Kotlin |
| UI | Jetpack Compose、Material 3 |
| 画面遷移 | Navigation 3、Kotlin Serialization |
| 状態管理 | ViewModel、StateFlow、Kotlin Coroutines |
| データベース | Room、KSP |
| カレンダー | Kizitonwose Calendar for Compose |
| テスト | JUnit、AndroidX Test、Espresso、Compose UI Test、Gradle Managed Devices |
| ビルド | Gradle Kotlin DSL、Version Catalog |
| CI/CD | GitHub Actions |

アプリは、Compose UIからViewModel、Repository、Roomへ処理を渡す階層構造です。RoomがTodoをSQLiteデータベースへ保存し、Flowを通してデータの変更をUIへ反映します。

```text
Compose UI → ViewModel → Repository → Room → SQLite
```

## 動作環境

- Android 10（API 29）以上
- JDK 17
- Android SDK 37
- Android Studio

## ビルド

リポジトリをクローンし、プロジェクトのルートでデバッグAPKをビルドします。

```bash
git clone https://github.com/twelnina/bunan-todo-app.git
cd bunan-todo-app
./gradlew assembleDebug
```

APKは `app/build/outputs/apk/debug/app-debug.apk` に生成されます。接続中の端末または起動中のエミュレーターへ直接インストールする場合は、次のコマンドを実行します。

```bash
./gradlew installDebug
```

## テスト

次のコマンドで、ローカルユニットテスト、Android Lint、Gradle Managed Deviceを使用したインストルメンテーションテストを実行できます。

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew pixel10Api36DebugAndroidTest
```

プルリクエストの作成時および`main`ブランチへのプッシュ時には、GitHub ActionsがテストとLintを実行し、デバッグAPKを自動でビルドします。

## リリースAPK

`v`から始まるタグをプッシュするか、GitHub ActionsからRelease APKワークフローを手動で実行すると、署名済みのリリースAPKがビルドされます。生成されたAPKは、ワークフロー実行結果の `bunan-todo-release-apk` アーティファクトからダウンロードできます。利用者向けに公開されたAPKは[GitHub Releases](https://github.com/twelnina/android-todo-app/releases)から入手できます。

## ライセンス

このプロジェクトは [MIT License](./LICENSE) のもとで公開しています。
