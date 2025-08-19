import { minOf, maxOf } from "./helper.js";

function appendSwitch(anchor, name, onchange) {
  anchor
    .append("div")
    .attr("class", "form-check form-switch")
    .append("label")
    .attr("class", "form-check-label")
    .text(name)
    .append("input")
    .attr("type", "checkbox")
    .attr("class", "form-check-input")
    .attr("value", name)
    .attr("checked", true)
    .on("change", (event) => onchange(event.target.value));
}

function appendSlider(anchor, min, max, onchange) {
  const values = [min, max];

  const label = anchor
    .append("label")
    .attr("class", "form-label w-100")
    .append("span")
    .text(`Range: ${min} - ${max}`);

  anchor
    .select("label")
    .append("input")
    .attr("type", "range")
    .attr("class", "form-range")
    .attr("min", min)
    .attr("max", max)
    .attr("value", max)
    .on("input", (event) => {
      values[1] = parseInt(event.target.value);
      label.text(`Range: ${minOf(values)} - ${maxOf(values)}`);
    })
    .on("change", () => {
      onchange(minOf(values), maxOf(values));
    });

  anchor
    .select("label")
    .append("input")
    .attr("type", "range")
    .attr("class", "form-range")
    .attr("min", min)
    .attr("max", max)
    .attr("value", min)
    .on("input", (event) => {
      values[0] = parseInt(event.target.value);
      label.text(`Range: ${minOf(values)} - ${maxOf(values)}`);
    })
    .on("change", () => {
      onchange(minOf(values), maxOf(values));
    });
}

export { appendSwitch, appendSlider };
