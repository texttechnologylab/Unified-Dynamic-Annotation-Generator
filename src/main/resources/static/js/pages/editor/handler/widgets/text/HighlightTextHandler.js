import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class HighlightTextHandler extends FormHandler {
  static defaults = {
    type: "HighlightText",
    title: "Highlight Text",
    generator: {},
    options: {},
    icon: "bi bi-card-text",
    w: 3,
    h: 3,
  };

  constructor(element, item) {
    super(element);

    this.item = item;
    this.span = this.element.querySelector("span");
  }

  init(modal, grid) {
    this.span.textContent = this.item.title;
    this.setIcon(this.item.icon);

    this.initButtons(modal, "Highlight Text Options", grid);
  }

  createForm() {
    const titleInput = this.createTextInput("title", "Title", this.item.title);
    const generatorInput = this.createTextInput(
      "generator",
      "Generator",
      this.item.generator.id
    );

    return createElement("form", { className: "dv-form-column" }, [
      titleInput,
      generatorInput,
    ]);
  }

  saveForm(form) {
    // Save form input
    const data = Object.fromEntries(new FormData(form));
    this.item.title = data.title;
    this.item.generator.id = data.generator;

    // Update Title
    this.span.textContent = data.title;
  }
}
