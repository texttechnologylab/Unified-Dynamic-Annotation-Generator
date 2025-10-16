import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class BarChartHandler extends FormHandler {
  static defaults = {
    type: "BarChart",
    title: "Bar Chart",
    generator: {},
    options: {
      horizontal: false,
    },
    icon: "bi bi-bar-chart",
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

    this.initButtons(modal, "Bar Chart Options", grid);
  }

  createForm() {
    const titleInput = this.createTextInput("title", "Title", this.item.title);
    const generatorInput = this.createTextInput(
      "generator",
      "Generator",
      this.item.generator.id
    );
    const orientationInput = this.createSelect(
      "orientation",
      "Orientation",
      ["horizontal", "vertical"],
      this.item.options.horizontal ? "horizontal" : "vertical"
    );

    return createElement("form", { className: "dv-form-column" }, [
      titleInput,
      generatorInput,
      orientationInput,
    ]);
  }

  saveForm(form) {
    // Save form input
    const data = Object.fromEntries(new FormData(form));
    this.item.title = data.title;
    this.item.generator.id = data.generator;
    this.item.options.horizontal = data.orientation === "horizontal";

    // Update Title
    this.span.textContent = data.title;
  }
}
