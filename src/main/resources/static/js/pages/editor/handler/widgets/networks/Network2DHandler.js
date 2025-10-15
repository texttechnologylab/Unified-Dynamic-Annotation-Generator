import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class Network2DHandler extends FormHandler {
  constructor(element, item) {
    super(element);

    this.item = item;
    this.span = this.element.querySelector("span");
  }

  init(modal, grid) {
    this.span.textContent = this.item.title;
    this.setIcon(this.item.icon);

    this.initButtons(modal, "Network 2D Options", grid);
  }

  createForm() {
    const titleInput = this.createTextInput("title", "Title", this.item.title);
    const generatorInput = this.createTextInput(
      "generator",
      "Generator",
      this.item.generator.id
    );
    const radiusInput = this.createNumberInput(
      "radius",
      "Node radius",
      this.item.options.radius,
      1,
      100
    );

    return createElement("form", { className: "dv-form-column" }, [
      titleInput,
      generatorInput,
      radiusInput,
    ]);
  }

  saveForm(form) {
    // Save form input
    const data = Object.fromEntries(new FormData(form));
    this.item.title = data.title;
    this.item.generator.id = data.generator;
    this.item.options.radius = data.radius;

    // Update Title
    this.span.textContent = data.title;
  }
}
