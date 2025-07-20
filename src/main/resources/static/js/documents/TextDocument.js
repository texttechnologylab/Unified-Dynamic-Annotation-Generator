import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";

export default class HighlightText extends D3Visualization {
  constructor(
    anchor,
    width,
    height,
    underline = true,
    bold = false,
    highlight = false
  ) {
    super(anchor, { top: 0, right: 0, bottom: 0, left: 0 }, width, height);

    this.underline = underline;
    this.bold = bold;
    this.highlight = highlight;

    d3.select(this.anchor).select("svg").remove();
    this.div = d3
      .select(this.anchor)
      .append("div")
      .attr("width", this.width)
      .attr("height", this.height)
      .style("text-align", "start");
  }

  create(data) {
    const spans = this.div
      .selectAll("span")
      .data(data)
      .join("span")
      .text((d) => d.text);

    spans
      .filter((item) => item.color)
      .style("font-weight", this.bold ? "bold" : null)
      .style("color", this.bold ? (item) => item.color : null)
      .style(
        "text-decoration",
        this.underline ? (item) => `underline 2px ${item.color}` : null
      )
      .style(
        "background-color",
        this.highlight ? (item) => item.color : "#f6f6f6"
      );

    spans
      .filter((item) => item.label)
      .style("cursor", "pointer")
      .on("mouseover", this.mouseover)
      .on("mouseleave", this.mouseleave);
  }

  mouseover = (event, data) => {
    const rect = event.target.getBoundingClientRect();

    this.tooltip
      .html(`<strong>${data.label}</strong>`)
      .style("color", data.color)
      .style("left", rect.left + window.scrollX + "px")
      .style("top", rect.top + window.scrollY - rect.height * 1.5 + "px")
      .style("opacity", 1);

    d3.select(event.target).style("opacity", 0.8);
  };

  mouseleave = (event) => {
    this.tooltip.style("opacity", 0);

    d3.select(event.target).style("opacity", 1);
  };
}
