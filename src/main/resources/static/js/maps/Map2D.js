import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";
import ExportHandler from "../utils/ExportHandler.js";

export default class Map2D extends D3Visualization {
  constructor(root, endpoint, { width, height }) {
    super(
      root,
      endpoint,
      { top: 0, right: 0, bottom: 0, left: 0 },
      width,
      height
    );
    this.handler = new ExportHandler(this.root.select(".dv-dropdown"), [
      "svg",
      "png",
      "csv",
      "json",
    ]);
  }

  async render(data) {
    this.clear();

    if (!data) {
      data = await this.fetch();
    }

    // https://raw.githubusercontent.com/holtzy/D3-graph-gallery/master/DATA/world.geojson
    d3.json("/js/maps/world.geojson").then((world) => {
      // Initialize zoom functionality
      const zoom = d3
        .zoom()
        .scaleExtent([0.8, 18])
        .translateExtent([
          [0, 0],
          [this.width, this.height],
        ])
        .on("zoom", (event) => {
          this.svg.select("g").attr("transform", event.transform);
        });
      this.svg.call(zoom);

      // Map and projection
      const projection = d3
        .geoEquirectangular()
        .fitSize([this.width, this.height], world);

      // A path generator
      const path = d3.geoPath().projection(projection);

      // Draw the map
      this.svg
        .select("g")
        .selectAll("path")
        .data(world.features)
        .join("path")
        .attr("d", path)
        .attr("fill", "#b8b8b8")
        .style("stroke", "#fff")
        .style("stroke-width", 0.1)
        .on("mouseover", (event) => this.mouseover(event.currentTarget))
        .on("mousemove", (event, data) =>
          this.mousemove(
            event.pageY,
            event.pageX + 20,
            `<strong>${data?.properties.name}</strong>`
          )
        )
        .on("mouseleave", (event) => this.mouseleave(event.currentTarget));

      // Draw the data
      this.svg
        .select("g")
        .selectAll()
        .data(data)
        .join("path")
        .attr("d", path)
        .style("fill", "none")
        .style("stroke", (item) => item.color)
        .style("stroke-width", 2)
        .on("mouseover", (event) => this.mouseover(event.currentTarget))
        .on("mousemove", (event, data) =>
          this.mousemove(
            event.pageY,
            event.pageX + 20,
            `<strong>${data.label}</strong>`
          )
        )
        .on("mouseleave", (event) => this.mouseleave(event.currentTarget));

      // Pass data to export handler
      this.handler.update(this.filter, data, this.svg.node());
    });
  }
}
