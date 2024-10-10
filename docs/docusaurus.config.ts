import type * as Preset from "@docusaurus/preset-classic";
import type { Config } from "@docusaurus/types";
import { themes as prismThemes } from "prism-react-renderer";

const config: Config = {
  title: "AdvantageKit",
  favicon: "img/favicon.ico",

  // Set the production url of your site here
  url: "https://docs.advantagekit.org",
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: "/",

  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"]
  },

  presets: [
    [
      "classic",
      {
        docs: {
          routeBasePath: "/",
          sidebarPath: "./sidebars.ts",
          sidebarCollapsed: true
        },
        theme: {
          customCss: "./src/css/custom.css"
        }
      } satisfies Preset.Options
    ]
  ],

  themeConfig: {
    image: "img/social.png",
    navbar: {
      title: "AdvantageKit Documentation",
      logo: {
        alt: "AdvantageKit Logo",
        src: "img/logo.png"
      },
      items: [
        {
          href: "https://github.com/Mechanical-Advantage/AdvantageKit",
          label: "GitHub",
          position: "right"
        }
      ]
    },
    colorMode: {
      disableSwitch: true,
      respectPrefersColorScheme: true
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ["java"]
    }
  } satisfies Preset.ThemeConfig
};

export default config;
