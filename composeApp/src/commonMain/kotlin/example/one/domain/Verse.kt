package example.one.domain

import kotlinx.serialization.Serializable

data class Bible(
    val languageCode: String,
    val languageEnglish: String,
    val languageNative: String,
    val voiceName: String,
    val tSettings: String,
) {
    fun shortName() = if (languageCode == "en") languageNative else languageCode.uppercase()
    fun filename() = if (languageCode == "en") "${languageCode}_${languageNative.lowercase()}" else languageCode
}

var bibles = listOf(
    Bible("en", "English", "KJV",  "en-GB-RyanNeural", "Text Settings"),
    Bible("en", "English", "BSB", "en-GB-RyanNeural", "Text Settings"),
    Bible("bn", "Bengali", "", "bn-IN-TanishaaNeural", "সেটিংস"),
    Bible("gu", "Gujarati", "", "gu-IN-DhwaniNeural", "સેટિંગ્સ"),
    Bible("hi", "Hindi", "", "hi-IN-SwaraNeural", "समायोजन"),
    Bible("kn", "Kannada", "", "kn-IN-GaganNeural", "ಸಂಯೋಜನೆಗಳು"),
    Bible("ml", "Malayalam", "", "ml-IN-SobhanaNeural", "ക്രമീകരണങ്ങൾ"),
    Bible("ne", "Nepali", "", "ne-NP-HemkalaNeural", "सेटिङहरू"),
    Bible("or", "Odia", "", "or-IN-SubhasiniNeural", "ସେଟିଂସମୂହ"),
    Bible("pa", "Punjabi", "", "pa-IN-OjasNeural", "ਸੈਟਿੰਗਾਂ"),
    Bible("ta", "Tamil", "", "ta-IN-PallaviNeural", "அமைப்புகள்"),
    Bible("te", "Telugu", "", "te-IN-ShrutiNeural", "సెట్టింగ్\\\\u200Cలు"),
)

const val BOOKS_COUNT = 66;

val engTitles = listOf(
    "Genesis",
    "Exodus",
    "Leviticus",
    "Numbers",
    "Deuteronomy",
    "Joshua",
    "Judges",
    "Ruth",
    "1 Samuel",
    "2 Samuel",
    "1 Kings",
    "2 Kings",
    "1 Chronicles",
    "2 Chronicles",
    "Ezra",
    "Nehemiah",
    "Esther",
    "Job",
    "Psalms",
    "Proverbs",
    "Ecclesiastes",
    "Song of Solomon",
    "Isaiah",
    "Jeremiah",
    "Lamentations",
    "Ezekiel",
    "Daniel",
    "Hosea",
    "Joel",
    "Amos",
    "Obadiah",
    "Jonah",
    "Micah",
    "Nahum",
    "Habakkuk",
    "Zephaniah",
    "Haggai",
    "Zechariah",
    "Malachi",
    "Matthew",
    "Mark",
    "Luke",
    "John",
    "Acts",
    "Romans",
    "1 Corinthians",
    "2 Corinthians",
    "Galatians",
    "Ephesians",
    "Philippians",
    "Colossians",
    "1 Thessalonians",
    "2 Thessalonians",
    "1 Timothy",
    "2 Timothy",
    "Titus",
    "Philemon",
    "Hebrews",
    "James",
    "1 Peter",
    "2 Peter",
    "1 John",
    "2 John",
    "3 John",
    "Jude",
    "Revelation",
)

val chapterSizes = listOf(
    50,
    40,
    27,
    36,
    34,
    24,
    21,
    4,
    31,
    24,
    22,
    25,
    29,
    36,
    10,
    13,
    10,
    42,
    150,
    31,
    12,
    8,
    66,
    52,
    5,
    48,
    12,
    14,
    3,
    9,
    1,
    4,
    7,
    3,
    3,
    3,
    2,
    14,
    4,
    28,
    16,
    24,
    21,
    28,
    16,
    16,
    13,
    6,
    6,
    4,
    4,
    5,
    3,
    6,
    4,
    3,
    1,
    13,
    5,
    5,
    3,
    5,
    1,
    1,
    1,
    22
)

@Serializable
data class Verse(
    val id: String,
    val bookIndex: Int,
    val bookName: String,
    val chapterIndex: Int,
    val verseIndex: Int,
    val heading: String,
    val text: String,
)
