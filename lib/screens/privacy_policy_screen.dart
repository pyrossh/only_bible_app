import "package:flutter/material.dart";
import "package:flutter_markdown/flutter_markdown.dart";
import "package:only_bible_app/utils.dart";

class PrivacyPolicyScreen extends StatelessWidget {
  const PrivacyPolicyScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Privacy Policy"),
      ),
      body: SafeArea(
        child: FutureBuilder(
          future: DefaultAssetBundle.of(context).loadString("assets/privacy-policy.md"),
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              return Markdown(
                data: snapshot.data!,
                onTapLink: (text, href, title) {
                  openUrl(context, href!);
                },
              );
            }
            return const Center(
              child: CircularProgressIndicator(),
            );
          },
        ),
      ),
    );
  }
}