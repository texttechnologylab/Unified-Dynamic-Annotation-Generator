import { createElement } from "../../../../../shared/modules/utils.js";
import FormHandler from "../../FormHandler.js";

export default class StaticImageHandler extends FormHandler {
  constructor(element, item) {
    super(element);

    this.item = item;
  }

  init(modal, grid) {
    // this.initButtons(modal, "Image Options", grid);
  }
}
