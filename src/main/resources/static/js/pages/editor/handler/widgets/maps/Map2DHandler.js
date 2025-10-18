import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class Map2DHandler extends FormHandler {
  static defaults = {
    type: "Map2D",
    title: "Map 2D",
    generator: { id: "" },
    options: {},
    icon: "bi bi-map",
    w: 4,
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

    this.initButtons(modal, "Map 2D Options", grid);
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
