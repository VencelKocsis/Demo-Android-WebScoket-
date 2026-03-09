package hu.bme.aut.android.demo.testsuite

import hu.bme.aut.android.demo.teamMatchOperation.ApplyForMatchTest
import hu.bme.aut.android.demo.teamMatchOperation.CancelApplyForMatchTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    ApplyForMatchTest::class,
    CancelApplyForMatchTest::class,
)
class ApplyAndCancelMatchTestSuite