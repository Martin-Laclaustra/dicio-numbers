package org.dicio.numbers.lang.it;

import org.dicio.numbers.formatter.NumberFormatter;
import org.dicio.numbers.util.MixedFraction;
import org.dicio.numbers.util.Utils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItalianFormatter extends NumberFormatter {

    final Map<Long, String> NUMBER_NAMES = new HashMap<Long, String>() {{
        put(0L, "zero");
        put(1L, "uno");
        put(2L, "due");
        put(3L, "tre");
        put(4L, "quattro");
        put(5L, "cinque");
        put(6L, "sei");
        put(7L, "sette");
        put(8L, "otto");
        put(9L, "nove");
        put(10L, "dieci");
        put(11L, "undici");
        put(12L, "dodici");
        put(13L, "tredici");
        put(14L, "quattordici");
        put(15L, "quindici");
        put(16L, "sedici");
        put(17L, "diciassette");
        put(18L, "diciotto");
        put(19L, "diciannove");
        put(20L, "venti");
        put(30L, "trenta");
        put(40L, "quaranta");
        put(50L, "cinquanta");
        put(60L, "sessanta");
        put(70L, "settanta");
        put(80L, "ottanta");
        put(90L, "novanta");
        put(100L, "cento");
        put(1000L, "mille");
        put(1000000L, "milione");
        put(1000000000L, "miliardo");
        put(1000000000000L, "bilione");
        put(1000000000000000L, "biliardo");
        put(1000000000000000000L, "trilione");
    }};

    final Map<Long, String> ORDINAL_NAMES = new HashMap<Long, String>() {{
        put(1L, "primo");
        put(2L, "secondo");
        put(3L, "terzo");
        put(4L, "quarto");
        put(5L, "quinto");
        put(6L, "sesto");
        put(7L, "settimo");
        put(8L, "ottavo");
        put(9L, "nono");
        put(10L, "decimo");
        put(11L, "undicesimo");
        put(12L, "dodicesimo");
        put(13L, "tredicesimo");
        put(14L, "quattordicesimo");
        put(15L, "quindicesimo");
        put(16L, "sedicesimo");
        put(17L, "diciassettesimo");
        put(18L, "diciottesimo");
        put(19L, "diciannovesimo");
        put(1000L, "millesimo");
        put(1000000L, "milionesimo");
        put(1000000000L, "miliardesimo");
        put(1000000000000L, "bilionesimo");
        put(1000000000000000L, "biliardesimo");
        put(1000000000000000000L, "trilionesimo");
    }};


    public ItalianFormatter() {
        super("config/it-it");
    }

    @Override
    public String niceNumber(final MixedFraction mixedFraction, final boolean speech) {
        if (speech) {
            final String sign = mixedFraction.negative ? "meno " : "";
            if (mixedFraction.numerator == 0) {
                return sign + pronounceNumber(mixedFraction.whole, 0, true, false, false);
            }

            String denominatorString;
            if (mixedFraction.denominator == 2) {
                denominatorString = "mezzo";
            } else {
                // use ordinal: only mezzo is exceptional
                denominatorString
                        = pronounceNumber(mixedFraction.denominator, 0, true, false, true);
            }

            final String numeratorString;
            if (mixedFraction.numerator == 1) {
                numeratorString = "un";
            } else {
                numeratorString = pronounceNumber(mixedFraction.numerator, 0, true, false, false);
                denominatorString = denominatorString.substring(0, denominatorString.length() - 1) + "i";
            }

            if (mixedFraction.whole == 0) {
                return sign + numeratorString + " " + denominatorString;
            } else {
                return sign + pronounceNumber(mixedFraction.whole, 0, true, false, false)
                        + " e " + numeratorString + " " + denominatorString;
            }

        } else {
            return niceNumberNotSpeech(mixedFraction);
        }
    }

    @Override
    public String pronounceNumber(double number,
                                  final int places,
                                  final boolean shortScale,
                                  final boolean scientific,
                                  final boolean ordinal) {
        // for italian shortScale is completely ignored

        if (number == Double.POSITIVE_INFINITY) {
            return "infinito";
        } else if (number == Double.NEGATIVE_INFINITY) {
            return "meno infinito";
        } else if (Double.isNaN(number)) {
            return "non un numero";
        }

        // also using scientific mode if the number is too big to be spoken fully. Checking against
        // the biggest double smaller than 10^21 = 1000 * 10^18, which is the biggest pronounceable
        // number, since e.g. 999.99 * 10^18 can be pronounced correctly.
        if (scientific || Math.abs(number) > 999999999999999934463d) {
            final String scientificFormatted = String.format("%E", number);
            final String[] parts = scientificFormatted.split("E", 2);
            final double power = Integer.parseInt(parts[1]);

            if (power != 0) {
                // This handles negatives of powers separately from the normal
                // handling since each call disables the scientific flag
                final double n = Double.parseDouble(parts[0]);
                return String.format("%s per dieci alla %s",
                        pronounceNumber(n, places, shortScale, false, false),
                        pronounceNumber(power, places, shortScale, false, false));
            }
        }

        final StringBuilder result = new StringBuilder();
        if (number < 0) {
            number = -number;
            // from here on number is always positive
            if (places != 0 || number >= 0.5) {
                // do not add minus if number will be rounded to 0
                result.append("meno ");
            }
        }

        final int realPlaces = Utils.decimalPlacesNoFinalZeros(number, places);
        final boolean numberIsWhole = realPlaces == 0;
        final boolean realOrdinal = ordinal && numberIsWhole;
        // if no decimal places to be printed, numberLong should be the rounded number
        final long numberLong = (long) number + (number % 1 >= 0.5 && numberIsWhole ? 1 : 0);

        if (realOrdinal && ORDINAL_NAMES.containsKey(numberLong)) {
            result.append(ORDINAL_NAMES.get(numberLong));

        } else if (!realOrdinal && NUMBER_NAMES.containsKey(numberLong)) {
            if (number > 1000) {
                result.append("un ");
            }
            result.append(NUMBER_NAMES.get(numberLong));

        } else {
            final List<Long> groups = Utils.splitByModulus(numberLong, 1000);
            final List<String> groupNames = new ArrayList<>();
            for (int i = 0; i < groups.size(); ++i) {
                final long z = groups.get(i);
                if (z == 0) {
                    continue; // skip 000 groups
                }
                String groupName = subThousand(z);

                if (i == 1) {
                    if (z == 1) {
                        groupName = "mille";
                    } else {
                        // use mila instead of mille
                        groupName += " mila";
                    }
                } else if (i != 0) {
                    // magnitude > 1000, so un is always there
                    if (z == 1) {
                        groupName = "un";
                    }

                    final long magnitude = Utils.longPow(1000, i);
                    groupName += " " + NUMBER_NAMES.get(magnitude);
                    if (z != 1) {
                        groupName = groupName.substring(0, groupName.length() - 1) + "i";
                    }
                }

                groupNames.add(groupName);
            }

            appendSplitGroups(result, groupNames);

            if (ordinal && numberIsWhole) { // not ordinal if not whole
                if (result.lastIndexOf("tre") != result.length() - 3) {
                    result.deleteCharAt(result.length() - 1);
                    if (result.lastIndexOf("mil") == result.length() - 3) {
                        result.append("l");
                    }
                }
                result.append("esimo");
            }
        }

        if (realPlaces > 0) {
            if (number < 1.0 && (result.length() == 0 || "meno ".contentEquals(result))) {
                result.append("zero"); // nothing was written before
            }
            result.append(" virgola");

            final String fractionalPart = String.format("%." + realPlaces + "f", number % 1);
            for (int i = 2; i < fractionalPart.length(); ++i) {
                result.append(" ");
                result.append(NUMBER_NAMES.get((long) (fractionalPart.charAt(i) - '0')));
            }
        }

        return result.toString();
    }

    @Override
    public String niceTime(final LocalTime time,
                           final boolean speech,
                           final boolean use24Hour,
                           final boolean showAmPm) {
        return "";
    }

    /**
     * @param n must be 0 <= n <= 999
     * @return the string representation of a number smaller than 1000
     */
    private String subThousand(final long n) {
        final StringBuilder builder = new StringBuilder();
        boolean requiresSpace = false; // whether a space needs to be added before the content
        if (n >= 100) {
            final long hundred = n / 100;
            if (hundred > 1) {
                builder.append(NUMBER_NAMES.get(hundred));
                builder.append(" ");
            }
            builder.append("cento");
            requiresSpace = true;
        }

        final long lastTwoDigits = n % 100;
        if (lastTwoDigits != 0 && NUMBER_NAMES.containsKey(lastTwoDigits)) {
            if (requiresSpace) {
                // this is surely true, but let's keep the space for consistency
                builder.append(" ");
            }
            builder.append(NUMBER_NAMES.get(lastTwoDigits));
        } else {
            final long ten = (n % 100) / 10;
            if (ten > 0) {
                if (requiresSpace) {
                    builder.append(" ");
                }
                builder.append(NUMBER_NAMES.get(ten * 10));
                requiresSpace = true;
            }

            final long unit = n % 10;
            if (unit > 0) {
                if (requiresSpace) {
                    builder.append(" ");
                }
                builder.append(NUMBER_NAMES.get(unit));
            }
        }

        return builder.toString();
    }

    /**
     * @param result the string builder to append the comma-separated group names to
     * @param groupNames the group names
     */
    private void appendSplitGroups(final StringBuilder result, final List<String> groupNames) {
        if (!groupNames.isEmpty()) {
            result.append(groupNames.get(groupNames.size() - 1));
        }

        for (int i = groupNames.size() - 2; i >= 0; --i) {
            result.append(", ");
            result.append(groupNames.get(i));
        }
    }
}
