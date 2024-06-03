import fs from "node:fs";
import { v2 } from '@google-cloud/translate';

const translate = new v2.Translate({
  key: "AIzaSyAYS5LdP5_i2AxIJprVQFYzb-7Nk2iJfv8",
});

const filename = "Bengali"
const code = "bn"
const outputLines = [];
const data = fs.readFileSync(`../assets/bibles/${filename}.txt`, "utf8");
const lines = data.split("\n");
for (const line of lines) {
  const arr = line.split("|");
  const book = parseInt(arr[0]);
  const chapter = parseInt(arr[1]);
  const verseNo = parseInt(arr[2]);
  let heading = arr[3];
  const verseText = arr.slice(4, arr.length).join("|");
  if (heading != "") {
    const [translation] = await translate.translate(heading, code);
    await new Promise((res) => {
      setTimeout(res, 2000);
    });
    heading = translation;
  }
  outputLines.push(`${book}|${chapter}|${verseNo}|${heading}|${verseText}`);
  if (chapter === 5) {
    break;
  }
}

const outputText = outputLines.join("\n")

fs.writeFileSync(`../assets/bibles/${filename}2.txt`, outputText, "utf8")