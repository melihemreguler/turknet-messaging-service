import 'app_locale.dart';

class AppStrings {
  final AppLocale locale;
  const AppStrings(this.locale);

  bool get _tr => locale == AppLocale.tr;

  // Auth — Login
  String get loginWelcome => _tr ? 'Tekrar hoş geldin' : 'Welcome back';
  String get loginSubtitle => _tr ? 'Devam etmek için giriş yap' : 'Sign in to continue';
  String get usernameHint => _tr ? 'Kullanıcı adı' : 'Username';
  String get passwordHint => _tr ? 'Parola' : 'Password';
  String get signInButton => _tr ? 'Giriş yap' : 'Sign in';
  String get noAccountPrompt => _tr ? 'Hesabın yok mu? Kayıt ol' : "Don't have an account? Register";
  String get usernameRequired => _tr ? 'Kullanıcı adı gerekli' : 'Username required';
  String get passwordRequired => _tr ? 'Parola gerekli' : 'Password required';
  String get loginFailed => _tr ? 'Giriş başarısız' : 'Login failed';

  // Auth — Register
  String get registerTitle => _tr ? 'Hesap oluştur' : 'Create account';
  String get registerSubtitle => _tr ? 'Bir dakika sürer' : 'It only takes a minute';
  String get emailHint => _tr ? 'E-posta' : 'Email';
  String get emailRequired => _tr ? 'E-posta gerekli' : 'Email required';
  String get emailInvalid => _tr ? 'Geçersiz e-posta' : 'Invalid email';
  String get passwordMinLength => _tr ? 'En az 6 karakter' : 'Min 6 characters';
  String get registerButton => _tr ? 'Kayıt ol' : 'Register';
  String get haveAccountPrompt => _tr ? 'Zaten hesabın var mı? Giriş yap' : 'Already have an account? Sign in';
  String get registerFailed => _tr ? 'Kayıt başarısız' : 'Register failed';

  // Inbox
  String get inboxTitle => _tr ? 'Mesajlar' : 'Messages';
  String get inboxFabNew => _tr ? 'Yeni' : 'New';
  String get inboxEmptyTitle => _tr ? 'Henüz konuşma yok' : 'No conversations yet';
  String get inboxEmptySubtitle => _tr ? '“Yeni”ye dokunarak sohbet başlat' : 'Tap “New” to start a chat';
  String get inboxLoadFailed => _tr ? 'Mesajlar yüklenemedi' : 'Failed to load inbox';
  String get retry => _tr ? 'Tekrar dene' : 'Retry';
  String get youPrefix => _tr ? 'Sen' : 'You';
  String get noMessagesYet => _tr ? 'Henüz mesaj yok' : 'No messages yet';
  String get more => _tr ? 'Daha fazla' : 'More';

  // New conversation
  String get newConversationTitle => _tr ? 'Yeni konuşma' : 'New conversation';
  String get to => _tr ? 'Kime' : 'To';
  String get startChat => _tr ? 'Sohbeti başlat' : 'Start chat';

  // Thread
  String get typeMessage => _tr ? 'Mesaj yaz' : 'Type a message';
  String get sendFailed => _tr ? 'Gönderilemedi' : 'Send failed';
  String get loadMessagesFailed => _tr ? 'Mesajlar yüklenemedi' : 'Failed to load messages';

  // Settings
  String get settingsTitle => _tr ? 'Ayarlar' : 'Settings';
  String get sectionAccount => _tr ? 'Hesap' : 'Account';
  String get sectionApp => _tr ? 'Uygulama' : 'App';
  String get profileEntryTitle => _tr ? 'Profil' : 'Profile';
  String get profileEntrySubtitle => _tr ? 'Hesabını yönet' : 'Manage your account';
  String get languageEntryTitle => _tr ? 'Dil' : 'Language';
  String get languagePickerTitle => _tr ? 'Dil seç' : 'Choose language';
  String get cancel => _tr ? 'Vazgeç' : 'Cancel';

  // Profile
  String get profileTitle => _tr ? 'Profil' : 'Profile';
  String get userIdLabel => _tr ? 'Kullanıcı ID' : 'User ID';
  String get sectionActivity => _tr ? 'Etkinlik' : 'Activity';
  String get activityEntryTitle => _tr ? 'Aktivitelerim' : 'My activity';
  String get activityEntrySubtitle => _tr ? 'Giriş ve hesap olayları' : 'Logins and account events';
  String get sectionSession => _tr ? 'Oturum' : 'Session';
  String get signOut => _tr ? 'Çıkış yap' : 'Sign out';
  String get signOutConfirmTitle => _tr ? 'Çıkış yapılsın mı?' : 'Sign out?';
  String get signOutConfirmBody => _tr
      ? 'Uygulamayı kullanmak için yeniden giriş yapman gerekir.'
      : 'You will need to sign in again to use the app.';
  String get versionLabel => _tr ? 'Sürüm' : 'Version';

  // Activity
  String get activityTitle => _tr ? 'Aktivitelerim' : 'My activity';
  String get activityEmpty => _tr ? 'Hiç aktivite yok' : 'No activity yet';
  String get loadMore => _tr ? 'Daha fazla yükle' : 'Load more';
  String get activityLoadFailed => _tr ? 'Aktivite yüklenemedi' : 'Failed to load activity';
  String get accountCreated => _tr ? 'Hesap oluşturuldu' : 'Account created';
  String get signedIn => _tr ? 'Giriş yapıldı' : 'Signed in';
  String get signInFailed => _tr ? 'Giriş başarısız' : 'Sign-in failed';
  String get statusSuccess => _tr ? 'Başarılı' : 'Success';
  String get statusFailed => _tr ? 'Başarısız' : 'Failed';
  String get unknownEvent => _tr ? 'Bilinmeyen olay' : 'Unknown event';

  // Errors (error_handler)
  String get networkError => _tr
      ? 'Ağ hatası. Bağlantını kontrol edip tekrar dene.'
      : 'Network error. Check your connection and try again.';
  String get serverError => _tr ? 'Sunucu hatası. Lütfen tekrar dene.' : 'Server error. Please try again.';
  String get somethingWentWrong => _tr ? 'Bir şeyler ters gitti' : 'Something went wrong';

  // Backend mesajları
  String get errUserNotFoundPrefix => _tr ? 'Kullanıcı bulunamadı' : 'User not found';
  String get errRecipientNotFoundPrefix => _tr ? 'Alıcı bulunamadı' : 'Recipient not found';
  String get errSenderNotFoundPrefix => _tr ? 'Gönderen bulunamadı' : 'Sender not found';
  String get errUsersNotFound => _tr ? 'Kullanıcılardan biri ya da ikisi bulunamadı' : 'One or both users not found';
  String get errAccessDeniedConversation => _tr ? 'Bu konuşmaya erişim yok' : 'Access denied to this conversation';
  String get errUserIdentifierMissing => _tr
      ? 'userId ya da username parametresinden biri verilmeli'
      : 'Either userId or username parameter must be provided';
  String get errUsernameTaken => _tr ? 'Bu kullanıcı adı zaten kullanılıyor' : 'Username already exists';
  String get errInvalidCredentials => _tr ? 'Kullanıcı adı veya parola hatalı' : 'Invalid credentials';
  String get errInvalidPassword => _tr ? 'Parola hatalı' : 'Invalid password';
  String get errMessageProcessing => _tr ? 'Mesaj işlenemedi' : 'Failed to process message';
  String get errDeliveryUnavailable => _tr
      ? 'Mesaj iletim servisi geçici olarak kullanılamıyor'
      : 'Message delivery service temporarily unavailable';
  String get errSessionCleanup => _tr ? 'Oturum temizleme başarısız' : 'Session cleanup operation failed';
  String get errInternal => _tr ? 'Sunucu hatası' : 'Internal server error';
  String get errNotFound => _tr ? 'Bulunamadı' : 'Not Found';
  String get errValidation => _tr ? 'Doğrulama hatası' : 'Validation failed';
  String get errAuthRequired => _tr ? 'Oturum açman gerekiyor' : 'Authentication required';
  String get errUserNotFoundGeneric => _tr ? 'Kullanıcı yok' : 'User does not exist';
}
