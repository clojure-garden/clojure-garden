module.exports = {
  plugins: [
    [
      require("babel-plugin-transform-imports"),
      {
        "@ant-design/icons": {
          transform: "@ant-design/icons/es/icons/${member}",
          preventFullImport: true,
        },
      },
    ],
  ],
};
