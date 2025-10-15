import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class PieChartHandler extends FormHandler {
  constructor(element, item) {
    super(element);

    this.item = item;
    this.span = this.element.querySelector("span");
  }

  init(modal, grid) {
    this.span.textContent = this.item.title;
    this.setIcon(this.item.icon);

    this.initButtons(modal, "Pie Chart Options", grid);
  }

  createForm() {
    const titleInput = this.createTextInput("title", "Title", this.item.title);
    const generatorInput = this.createTextInput(
      "generator",
      "Generator",
      this.item.generator.id
    );
    const holeInput = this.createNumberInput(
      "hole",
      "Hole (Doughnut)",
      this.item.options.hole,
      0,
      1000
    );

    return createElement("form", { className: "dv-form-column" }, [
      titleInput,
      generatorInput,
      holeInput,
    ]);
  }

  saveForm(form) {
    // Save form input
    const data = Object.fromEntries(new FormData(form));
    this.item.title = data.title;
    this.item.generator.id = data.generator;
    this.item.options.hole = data.hole;

    // Update Title
    this.span.textContent = data.title;
  }
}
