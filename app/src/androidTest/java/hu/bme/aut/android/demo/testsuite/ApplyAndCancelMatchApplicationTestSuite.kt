package hu.bme.aut.android.demo.testsuite

import hu.bme.aut.android.demo.teamMatch.ApplyForMatchTest
import hu.bme.aut.android.demo.teamMatch.CancelApplyForMatchTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Itt adjuk meg a futtatási sorrendet:
    // 1. Mindenki jelentkezik az összes meccsre
    ApplyForMatchTest::class,

    // 2. Mindenki visszavonja a jelentkezését az összes meccsről
    CancelApplyForMatchTest::class
)
class ApplyAndCancelMatchApplicationTestSuite {}