module.exports = function (config) {
  config.set({
    browsers: ["ChromeHeadlessNoSandbox"],
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: "ChromeHeadless",
        flags: ["--no-sandbox"],
      },
    },
    basePath: "public",
    files: ["test/assets/ci.js"],
    frameworks: ["cljs-test"],
    plugins: ["karma-cljs-test", "karma-chrome-launcher"],
    colors: true,
    logLevel: config.LOG_INFO,
    client: {
      args: ["shadow.test.karma.init"],
      singleRun: true,
    }
  });
};
