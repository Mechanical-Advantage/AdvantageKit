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
  onBrokenMarkdownLinks: "throw",

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "en",
    locales: ["en"]
  },

  // Copy built Javadoc files
  staticDirectories: ["static", "../akit/build/docs"],

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

  plugins: [
    [
      "@docusaurus/plugin-client-redirects",
      {
        redirects: [
          {
            to: "/getting-started/template-projects",
            from: "/category/template-projects"
          },
          {
            to: "/getting-started/common-issues",
            from: "/category/common-issues"
          },
          {
            to: "/data-flow/recording-inputs",
            from: "/category/recording-inputs"
          },
          {
            to: "/theory/high-frequency-odometry",
            from: "/getting-started/template-projects/high-frequency-odometry"
          },
          {
            to: "/theory/log-replay-comparison",
            from: "/getting-started/what-is-advantagekit/log-replay-comparison"
          },
          {
            to: "/theory/deterministic-timestamps",
            from: "/data-flow/deterministic-timestamps"
          }
        ]
      }
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
          href: "pathname:///javadoc",
          label: "API Docs",
          position: "right"
        },
        {
          href: "https://github.com/Mechanical-Advantage/AdvantageKit",
          label: "GitHub",
          position: "right"
        }
      ]
    },
    footer: {
      copyright: "Copyright © 2021-2025 Littleton Robotics",
      links: [
        {
          label: "Littleton Robotics",
          href: "https://littletonrobotics.org"
        },
        {
          label: "AdvantageScope",
          href: "https://docs.advantagescope.org"
        },
        {
          label: "WPILib Docs",
          href: "https://docs.wpilib.org"
        }
      ]
    },
    docs: {
      sidebar: {
        hideable: true
      }
    },
    colorMode: {
      disableSwitch: false,
      respectPrefersColorScheme: true
    },
    prism: {
      theme: prismThemes.gruvboxMaterialDark,
      additionalLanguages: ["java", "groovy"]
    },
    algolia: {
      appId: "7JW2R5AY94",
      apiKey: "34dce56eab484e7f5d69f9a71f44f3eb",
      indexName: "advantagekit"
    }
  } satisfies Preset.ThemeConfig
};

export default config;
