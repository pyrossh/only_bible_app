import "dart:convert";
import "dart:developer";
import "package:firebase_crashlytics/firebase_crashlytics.dart";
import "package:firebase_storage/firebase_storage.dart";
import "package:flutter/material.dart";
import "package:flutter/services.dart";
import "package:get_storage/get_storage.dart";
import "package:just_audio/just_audio.dart";
import "package:only_bible_app/atom.dart";
import "package:only_bible_app/dialog.dart";
import "package:only_bible_app/models.dart";
import "package:only_bible_app/theme.dart";
import "package:only_bible_app/utils.dart";
import "package:only_bible_app/navigation.dart";
import "package:only_bible_app/widgets/note_sheet.dart";

final box = GetStorage("only-bible-app-prefs");
final player = AudioPlayer();
final noteTextController = TextEditingController();

initState() async {
  await ensureAtomsInitialized(box);
}

final Atom<bool> firstOpen = Atom<bool>(
  key: "firstOpen",
  initialValue: true,
  set: () {
    firstOpen.value = false;
  },
);

final Atom<String> languageCode = Atom<String>(
  key: "languageCode",
  initialValue: "en",
  update: (String v) {
    languageCode.value = v;
  },
);

final Atom<String> bibleName = Atom<String>(
  key: "bibleName",
  initialValue: "English",
  update: (String v) {
    bibleName.value = v;
  },
);

final Atom<Bible> bible = Atom<Bible>(
  key: "bible",
  persist: false,
  initialValue: Bible(name: "English"),
  update: (Bible v) {
    bible.value = v;
  },
);

final bibleCache = Atom<Future<Bible?>?>(
  key: "bible",
  persist: false,
  initialValue: null,
);

updateCurrentBible(BuildContext context, Locale l, String name) async {
  languageCode.value = l.languageCode;
  bibleName.update!(name);
  bibleCache.value = loadBible(name);
}

Future<Bible> getBibleFromAsset(String file) async {
  final bytes = await rootBundle.load("assets/bibles/$file.txt");
  final books = getBibleFromText(utf8.decode(bytes.buffer.asUint8List(), allowMalformed: false));
  // await Future.delayed(Duration(seconds: 1));
  return Bible.withBooks(
    name: bibleName.value,
    books: books,
  );
}

Future<Bible?> loadBible(String name) async {
  print("loadBible ${name}");
  return getBibleFromAsset(name).then((value) {
    bible.update!(value);
    return value;
  });
}
// Trace customTrace;
// if (!isDesktop()) {
//   customTrace = FirebasePerformance.instance.newTrace("loadBible");
//   await customTrace.start();
// }
// bibleLoading = Future.delayed(const Duration(seconds: 2)).then((value) => getBibleFromAsset(bibleName.value));
// if (!isDesktop()) {
//   await customTrace.stop();
// }

final Atom<bool> engTitles = Atom<bool>(
  key: "engTitles",
  initialValue: false,
  set: () {
    engTitles.value = !engTitles.value;
  },
);

final Atom<bool> darkMode = Atom<bool>(
  key: "darkMode",
  initialValue: false,
  set: () {
    darkMode.value = !darkMode.value;
    updateStatusBar(darkMode.value);
  },
);

final Atom<bool> fontBold = Atom<bool>(
  key: "fontBold",
  initialValue: false,
  set: () {
    fontBold.value = !fontBold.value;
  },
);

final Atom<double> textScale = Atom<double>(
  key: "textScale",
  initialValue: 0,
  update: (double v) {
    textScale.value += v;
  },
);

final Atom<int> savedBook = Atom<int>(
  key: "savedBook",
  initialValue: 0,
  update: (int v) {
    savedBook.value = v;
  },
);

final Atom<int> savedChapter = Atom<int>(
  key: "savedChapter",
  initialValue: 0,
  update: (int v) {
    savedChapter.value = v;
  },
);

final Atom<bool> isPlaying = Atom<bool>(
  key: "isPlaying",
  initialValue: false,
  persist: false,
  update: (bool v) {
    isPlaying.value = v;
  },
);

final Atom<List<Verse>> selectedVerses = Atom<List<Verse>>(
  key: "selectedVerses",
  initialValue: [],
  persist: false,
  update: (List<Verse> verses) {
    selectedVerses.value = verses;
    // selectedVerses.notifyChanged();
  },
);

void clearSelections() {
  selectedVerses.value.clear();
}

Color? getHighlight(Verse v) {
  final key = "${v.book}:${v.chapter}:${v.index}:highlight";
  if (box.hasData(key)) {
    // box.remove(key);
    // print(box.read(key));
    final index = box.read<int>(key);
    if (index == null) {
      return null;
    }
    return darkMode.value ? darkHighlights[index] : lightHighlights[index];
  }
  return null;
}

void setHighlight(BuildContext context, int index) {
  for (final v in selectedVerses.value) {
    box.write("${v.book}:${v.chapter}:${v.index}:highlight", index);
  }
  box.save();
  hideActions(context);
}

void removeHighlight(BuildContext context) {
  for (final v in selectedVerses.value) {
    box.remove("${v.book}:${v.chapter}:${v.index}:highlight");
  }
  box.save();
  hideActions(context);
}

bool isVerseSelected(Verse v) {
  return selectedVerses.value.any((el) => el.book == v.book && el.chapter == v.chapter && el.index == v.index);
}

bool watchVerseSelected(BuildContext context, Verse v) {
  return selectedVerses.watch(context).any((el) => el.book == v.book && el.chapter == v.chapter && el.index == v.index);
}

void onVerseSelected(BuildContext context, Verse v) {
  if (isVerseSelected(v)) {
    selectedVerses.update!(selectedVerses.value.removeBy((it) => it.index == v.index).toList());
  } else {
    selectedVerses.update!(selectedVerses.value.addBy(v).toList());
  }
  if (selectedVerses.value.isNotEmpty) {
    showActions(context);
  }
  if (selectedVerses.value.isEmpty) {
    hideActions(context);
  }
}

TextStyle getHighlightStyle(BuildContext context, Verse v) {
  if (watchVerseSelected(context, v)) {
    return TextStyle(
      backgroundColor: darkMode.value ? Colors.grey.shade800 : Colors.grey.shade200,
    );
  }
  if (darkMode.watch(context)) {
    // return TextStyle(
    //   color: getHighlight(v) ?? context.theme.colorScheme.onBackground,
    // );
    return TextStyle(
      backgroundColor: getHighlight(v)?.withOpacity(0.7),
      color: getHighlight(v) != null ? Colors.white : context.theme.colorScheme.onBackground,
    );
  }
  return TextStyle(
    backgroundColor: getHighlight(v) ?? context.theme.colorScheme.background,
  );
}

clearEvents(BuildContext context) {
  if (isPlaying.value) {
    pause();
  }
  hideActions(context);
}

pause() async {
  await player.pause();
  isPlaying.value = false;
}

onPlay(BuildContext context) async {
  final versesToPlay = List.from(selectedVerses.value);
  if (isPlaying.value) {
    pause();
  } else {
    isPlaying.value = true;
    for (final v in versesToPlay) {
      final book = (v.book + 1).toString().padLeft(2, "0");
      final chapter = (v.chapter + 1).toString().padLeft(3, "0");
      final verseNo = (v.index + 1).toString().padLeft(3, "0");
      final pathname = "${bibleName.value}/$book-$chapter-$verseNo.mp3";
      try {
        final url = await FirebaseStorage.instance.ref(pathname).getDownloadURL();
        await player.setUrl(url);
        await player.play();
        await player.stop();
      } catch (err) {
        log("Could not play audio", name: "play", error: (err.toString(), pathname));
        FirebaseCrashlytics.instance.recordFlutterError(FlutterErrorDetails(exception: (err.toString(), pathname)));
        if (context.mounted) {
          showError(context, context.l.audioError);
        }
        return;
      } finally {
        pause();
      }
    }
  }
}

bool hasNote(Verse v) {
  return box.hasData("${v.book}:${v.chapter}:${v.index}:note");
}

showNoteField(BuildContext context, Verse v) {
  final noteText = box.read("${v.book}:${v.chapter}:${v.index}:note") ?? "";
  noteTextController.text = noteText;
  showModalBottomSheet(
    context: context,
    isDismissible: true,
    enableDrag: true,
    showDragHandle: true,
    useSafeArea: true,
    isScrollControlled: true,
    builder: (context) => NoteSheet(verse: v),
  );
}

saveNote(BuildContext context, Verse v) {
  final note = noteTextController.text;
  box.write("${v.book}:${v.chapter}:${v.index}:note", note);
  box.save();
  hideNoteField(context);
  hideActions(context);
}

deleteNote(BuildContext context, Verse v) {
  box.remove("${v.book}:${v.chapter}:${v.index}:note");
  box.save();
  hideNoteField(context);
  // TODO: hack to re-render this page
  selectedVerses.notifyChanged();
}

hideNoteField(BuildContext context) {
  Navigator.of(context).pop();
}
