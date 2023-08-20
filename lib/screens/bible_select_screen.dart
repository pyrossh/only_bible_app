import "package:flutter/material.dart";
import "package:only_bible_app/models.dart";
import "package:only_bible_app/providers/app_model.dart";
import "package:only_bible_app/widgets/scaffold_menu.dart";
import "package:only_bible_app/widgets/sliver_heading.dart";
import "package:only_bible_app/widgets/sliver_tile_grid.dart";

class BibleSelectScreen extends StatelessWidget {
  const BibleSelectScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final model = AppModel.of(context);
    return ScaffoldMenu(
      child: CustomScrollView(
        physics: const BouncingScrollPhysics(),
        slivers: [
          const SliverHeading(title: "Bibles", showClose: true),
          SliverTileGrid(
            listType: ListType.large,
            children: List.of(
              bibles.map((bible) {
                return TextButton(
                  child: Text(bible.name),
                  onPressed: () {
                    model.updateCurrentBible(context, bible.id);
                    Navigator.of(context).pop();
                  },
                );
              }),
            ),
          ),
        ],
      ),
    );
  }
}
