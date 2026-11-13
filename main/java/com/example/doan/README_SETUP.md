# Hướng dẫn tích hợp

## 1. Cấu trúc Firestore
- `users/{uid}`: name, email, role ("student" | "teacher")
- `quizzes/{id}`: title, subject, durationMinutes, questionCount, status (Upcoming|Live|Completed), ownerId
- `quizzes/{id}/questions/{qid}`: text, optionA-D, order        (KHÔNG có đáp án đúng)
- `quizzes/{id}/answerKeys/{qid}`: correct ("A".."D")
- `results/{id}`: quizId, quizTitle, userId, userName, score (thang 10), correctCount, totalQuestions, timeUp, submittedAt, answers{qid: "A".."D"}

Đề cũ tạo tay trên console cần thêm trường `ownerId` = uid giáo viên thì mới hiện ở màn hình giáo viên,
và mỗi câu hỏi cần một document tương ứng trong `answerKeys` (cùng id) thì mới chấm được.

## 2. build.gradle (module app)
```
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.appcompat:appcompat:1.7.0")
implementation("androidx.core:core-ktx:1.13.1")
```
Cần plugin `com.google.gms.google-services` và file `google-services.json`.
Bật Authentication > Email/Password trong Firebase console, rồi dán `firestore.rules` vào tab Rules.

## 3. AndroidManifest.xml
Đặt Splash làm màn hình khởi động, khai báo tất cả Activity mới:
```xml
<uses-permission android:name="android.permission.INTERNET" />

<activity android:name=".ui.auth.SplashActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name=".ui.auth.WelcomeActivity" />
<activity android:name=".ui.auth.LoginActivity" />
<activity android:name=".ui.auth.RegisterActivity" />
<activity android:name=".ui.student.StudentDashboardActivity" />
<activity android:name=".ui.student.ExamIntroActivity" />
<activity android:name=".ui.student.DoExamActivity" />
<activity android:name=".ui.student.ExamResultActivity" />
<activity android:name=".ui.student.ResultActivity" />
<activity android:name=".ui.student.ExamHistoryActivity" />
<activity android:name=".ui.student.MyQuizzesActivity" />
<activity android:name=".ui.student.ProfileActivity" />
<activity android:name=".ui.teacher.TeacherDashboardActivity" />
<activity android:name=".ui.teacher.QuizFormActivity" />
<activity android:name=".ui.teacher.QuestionFormActivity" />
<activity android:name=".ui.teacher.TeacherResultsActivity" />
```
Chỉ `SplashActivity` được có `intent-filter` MAIN/LAUNCHER (các Activity khác không được có). `MainActivity` giờ chỉ chuyển sang Splash, có thể xoá khai báo nó trong manifest nếu muốn. Xoá khai báo `MainActivity` (template Compose, không còn dùng) và `ExamActivity` (đã xoá).

## 4. Lưu ý
- Menu "Đề chưa làm / Lịch sử thi / Hồ sơ" của sinh viên nằm trên ActionBar; theme NoActionBar thì sẽ không thấy.
  Khi đó thêm nút vào `activity_student_dashboard.xml` rồi gọi `startActivity` tới các Activity trên.
- Đáp án đúng nằm ở `answerKeys` và chấm điểm chạy trên máy sinh viên, rules chỉ chặn ghi chứ không chặn đọc.
  Đủ cho đồ án; muốn chống gian lận thật sự thì chuyển bước chấm sang Cloud Functions.
- Đăng ký cho phép tự chọn vai trò Giáo viên (tiện cho demo).
