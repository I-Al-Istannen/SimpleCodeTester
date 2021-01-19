import { IoLine, IoLineType } from "@/store/types";

/**
 * Guesses the type of a line.
 *
 * @param line the line to guess the type for
 */
export function guessLineType(line: string): IoLineType {
  if (line.startsWith("#")) {
    return IoLineType.OTHER;
  }
  if (line.startsWith("> ")) {
    return IoLineType.INPUT;
  }
  if (line.startsWith("$$ ")) {
    return IoLineType.PARAMETER;
  }

  if (line.startsWith("<e")) {
    return IoLineType.ERROR;
  }

  return IoLineType.OUTPUT;
}

/**
 *
 * @param code the code to highlight
 */
export function highlight(code: IoLine[]) {
  const resultSpans = code.map((line) => {
    const result: string[] = [];

    // Dirty, but overwrite the type here. This is not really an error line, just an error output matcher.
    // This means the backend sets the type to "OUTPUT", but for displaying it is nicer to work with "ERROR"
    let type: IoLineType = line.lineType;
    if (line.content === "<e") {
      type = IoLineType.ERROR;
    }

    if (getPrefix(line).length > 0) {
      result.push(`<span class="prefix">${escapeHtml(getPrefix(line))}</span>`);
    }
    result.push(
      `<span class="rest">${escapeHtml(getRest(line) || "\n")}</span>`
    );

    const inner = result.join("");

    const classes = ["line", "line-" + type].join(" ");
    return `<span class="${classes}">${inner}</span>`;
  });
  return resultSpans.join("");
}

/**
 * Escapes all HTML in a given string.
 *
 * @param input the input string to escape
 */
function escapeHtml(input: string): string {
  const p = document.createElement("p");
  p.appendChild(document.createTextNode(input));
  return p.innerHTML;
}

function getPrefix(line: IoLine) {
  const regex = /^(> |<.|# |\$\$ )/;
  const match = regex.exec(line.content);
  if (match) {
    return match[1];
  }

  return "";
}

function getRest(line: IoLine) {
  let content: string;
  if (getPrefix(line) !== "") {
    content = line.content.substring(getPrefix(line).length);
  } else {
    content = line.content;
  }

  if (
    line.lineType == IoLineType.INPUT ||
    line.lineType == IoLineType.PARAMETER
  ) {
    return replaceSpacesWithSpecialChar(content);
  }

  return content;
}

function replaceSpacesWithSpecialChar(input: string) {
  return input.replace(/ /g, "␣");
}
