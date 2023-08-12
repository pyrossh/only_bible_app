import "package:flutter/material.dart";
import "package:only_bible_app/state.dart";

class PlayButton extends StatelessWidget {
  const PlayButton({super.key});

  @override
  Widget build(BuildContext context) {
    final model = ChapterViewModel.of(context);
    final icon = model.isPlaying ? Icons.pause_circle_filled : Icons.play_circle_fill;
    final size = isWide(context) ? 28.0 : 42.0;

    return IconButton(
      icon: Icon(icon, size: size),
      onPressed: () {
        model.onPlay(context);
      },
    );
  }
}