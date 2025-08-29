export default class ExportHandler {
  constructor(node, formats) {
    this.serializer = new XMLSerializer();
    this.storage = {
      json: null,
      svg: null,
    };

    const icons = {
      svg: "bi bi-file-earmark-code",
      png: "bi bi-image",
      csv: "bi bi-table",
      json: "bi bi-braces",
    };

    formats.forEach((format) => {
      const btn = node
        .append("button")
        .attr("class", "dv-btn")
        .attr("type", "button")
        .on("click", () => this.prepareExport(format));

      btn.append("i").attr("class", icons[format]);
      btn.append("span").text("Export as " + format);
    });
  }

  update(json, svg) {
    this.storage.json = json;
    this.storage.svg = svg;
  }

  prepareExport(format) {
    switch (format) {
      case "svg":
        this.exportSVG();
        break;
      case "png":
        this.exportPNG();
        break;
      case "csv":
        this.exportCSV();
        break;
      case "json":
        this.exportJSON();
        break;
    }
  }

  exportSVG() {
    const header = '<?xml version="1.0" standalone="no"?>\r\n';
    const str = this.serializer.serializeToString(this.storage.svg);
    const url = this.createURL(header + str, "image/svg+xml");

    this.downloadURL(url, "chart.svg");
  }

  exportPNG() {
    const str = this.serializer.serializeToString(this.storage.svg);
    const url = this.createURL(str, "image/svg+xml");
    const img = new Image();

    img.onload = () => {
      const bbox = this.storage.svg.getBBox();

      const canvas = document.createElement("canvas");
      canvas.width = bbox.width;
      canvas.height = bbox.height;

      const context = canvas.getContext("2d");
      context.drawImage(img, 0, 0, bbox.width, bbox.height);

      this.downloadURL(canvas.toDataURL(), "chart.png");
    };
    img.src = url;
  }

  exportCSV() {
    const json = this.storage.json;
    const keys = Object.keys(json[0]);

    const escape = (value) => {
      const str = String(value);
      return /[",\n]/.test(str) ? '"' + str.replace(/"/g, '""') + '"' : str;
    };

    const header = keys.join(",");
    const rows = json.map((o) => keys.map((k) => escape(o[k])).join(","));

    const str = [header, ...rows].join("\r\n");
    const url = this.createURL(str, "text/csv");

    this.downloadURL(url, "data.csv");
  }

  exportJSON() {
    const str = JSON.stringify(this.storage.json, null, 2);
    const url = this.createURL(str, "application/json");

    this.downloadURL(url, "data.json");
  }

  createURL(str, type) {
    return URL.createObjectURL(new Blob([str], { type }));
  }

  downloadURL(url, name) {
    const a = document.createElement("a");
    a.href = url;
    a.download = name;

    a.click();

    a.remove();
    URL.revokeObjectURL(url);
  }
}
