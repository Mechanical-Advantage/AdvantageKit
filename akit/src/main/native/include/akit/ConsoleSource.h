#pragma once
#include <string>
#include <thread>
#include <queue>
#include <sstream>
#include <iostream>

namespace akit {

class ConsoleSource {
    public:
    virtual std::string getNewData() = 0;

    class Simulator : public ConsoleSource {
        public:
        Simulator() {
            std::cout.rdbuf(splitCout.rdbuf());
            std::cout.rdbuf(splitCerr.rdbuf());
        }

        private:
        class SplitBuffer : public std::streambuf {
            public:
            SplitBuffer(std::streambuf* original, std::ostringstream& capture)
                : original{original}, capture{capture} {}
            
            protected:
            int overflow(int c) override {
                if (c == EOF) return !EOF;

                original->sputc(c);
                capture.put(static_cast<char>(c));
                return c;
            }

            private:
            std::streambuf* original;
            std::ostringstream& capture;
        };

        std::streambuf* originalCout{std::cout.rdbuf()};
        std::streambuf* originalCerr{std::cout.rdbuf()};

        std::ostringstream capturedCout;
        std::ostringstream capturedCerr;

        SplitBuffer splitCout{originalCout, capturedCout};
        SplitBuffer splitCerr{originalCerr, capturedCerr};

        size_t coutPos = 0;
        size_t cerrPos = 0;
    };
};

}