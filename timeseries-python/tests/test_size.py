import pytest
from analysis.screens.size import kelly


def test_kelly_caps_and_scales_to_risk_fraction():
    assert kelly(0.01, 0.02) == pytest.approx(1.0)
    assert kelly(0.01, 0.5, risk_fraction=1.0) == pytest.approx(0.04)


def test_kelly_handles_edge_cases():
    assert kelly(0.05, 0) == 0.0
    assert kelly(-0.01, 0.02) == 0.0
