import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class StaticTextHandler extends FormHandler {
  static defaults = {
    type: "StaticText",
    title: "Text",
    text: "The quick brown fox jumps over the lazy dog.",
    options: {
      style: "text-start fs-5 fw-normal fst-normal text-decoration-none",
    },
    icon: "bi bi-fonts",
    w: 2,
    h: 1,
  };

  constructor(element, item) {
    super(element);

    this.item = item;
    this.div = this.element.querySelector("#static-text");
  }

  init(modal, grid) {
    this.div.textContent = this.item.text;
    this.div.className = this.item.options.style;

    this.initButtons(modal, "Text Options", grid);
  }

  createForm() {
    const styles = this.item.options.style.split(" ");

    const textInput = this.createTextInput("text", "Text", this.item.text);
    const alignInput = this.createSelect(
      "align",
      "Text alignment",
      ["text-start", "text-center", "text-end"],
      styles[0]
    );
    const sizeInput = this.createSelect(
      "size",
      "Font size",
      ["fs-1", "fs-2", "fs-3", "fs-4", "fs-5", "fs-6"],
      styles[1]
    );
    const weightInput = this.createSelect(
      "weight",
      "Font weight",
      ["fw-normal", "fw-bold"],
      styles[2]
    );
    const styleInput = this.createSelect(
      "style",
      "Font style",
      ["fst-normal", "fst-italic"],
      styles[3]
    );
    const decorationInput = this.createSelect(
      "decoration",
      "Text decoration",
      [
        "text-decoration-none",
        "text-decoration-underline",
        "text-decoration-line-through",
      ],
      styles[4]
    );
    const titleInput = this.createTextInput(
      "title",
      "Tooltip",
      this.item.title
    );

    return createElement("form", { className: "dv-form-column" }, [
      textInput,
      alignInput,
      sizeInput,
      weightInput,
      styleInput,
      decorationInput,
      titleInput,
    ]);
  }

  saveForm(form) {
    // Save form input
    const data = Object.fromEntries(new FormData(form));
    this.item.text = data.text;
    this.item.options.style = `${data.align} ${data.size} ${data.weight} ${data.style} ${data.decoration}`;
    this.item.title = data.title;

    this.div.textContent = this.item.text;
    this.div.className = this.item.options.style;
  }
}
