#!/usr/bin/env python3
"""
receive_skill.py - Pull and install skills from a GitHub repository into the Manus skills directory.

Usage:
    python3 receive_skill.py <github_repo> [--version <version_or_tag>] [--skill <skill_name>]

Examples:
    python3 receive_skill.py tywade1980/manus-DRS-skills
    python3 receive_skill.py tywade1980/manus-DRS-skills --version v1.0.0
    python3 receive_skill.py tywade1980/manus-DRS-skills --skill caroline-ai
"""

import argparse
import json
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SKILLS_DIR = Path("/home/ubuntu/skills")
VERSIONS_FILE = SKILLS_DIR / ".skill_versions.json"


def load_versions() -> dict:
    """Load the current skill versions registry."""
    if VERSIONS_FILE.exists():
        with open(VERSIONS_FILE, "r") as f:
            return json.load(f)
    return {}


def save_versions(versions: dict):
    """Save the updated skill versions registry."""
    with open(VERSIONS_FILE, "w") as f:
        json.dump(versions, f, indent=2)
    print(f"  [✓] Updated skill versions registry at {VERSIONS_FILE}")


def run_cmd(cmd: list, cwd: str = None, capture: bool = True) -> tuple[int, str, str]:
    """Run a shell command and return (returncode, stdout, stderr)."""
    result = subprocess.run(
        cmd,
        cwd=cwd,
        capture_output=capture,
        text=True
    )
    return result.returncode, result.stdout.strip(), result.stderr.strip()


def clone_repo(repo: str, target_dir: str, version: str = None) -> bool:
    """Clone a GitHub repository to a target directory."""
    print(f"  [→] Cloning {repo} ...")
    cmd = ["gh", "repo", "clone", repo, target_dir, "--", "--depth=1"]
    if version:
        cmd = ["gh", "repo", "clone", repo, target_dir, "--", "--depth=1", f"--branch={version}"]
    
    rc, out, err = run_cmd(cmd)
    if rc != 0:
        # Try HTTPS fallback
        print(f"  [!] gh clone failed, trying git clone ...")
        https_url = f"https://github.com/{repo}.git"
        cmd = ["git", "clone", "--depth=1"]
        if version:
            cmd += ["-b", version]
        cmd += [https_url, target_dir]
        rc, out, err = run_cmd(cmd)
        if rc != 0:
            print(f"  [✗] Clone failed: {err}")
            return False
    
    print(f"  [✓] Repository cloned successfully.")
    return True


def get_repo_version(repo_dir: str) -> str:
    """Get the current commit hash of the cloned repo."""
    rc, out, err = run_cmd(["git", "rev-parse", "--short", "HEAD"], cwd=repo_dir)
    if rc == 0:
        return out
    return "unknown"


def discover_skills(repo_dir: str) -> list[dict]:
    """
    Discover all skills in the repository.
    A skill is any directory containing a SKILL.md file.
    Also supports a flat structure where SKILL.md is at the root.
    """
    skills = []
    repo_path = Path(repo_dir)
    
    # Check root-level SKILL.md (single-skill repo)
    root_skill = repo_path / "SKILL.md"
    if root_skill.exists():
        # Infer skill name from repo name
        skill_name = repo_path.name.replace("manus-", "").replace("-skills", "")
        skills.append({
            "name": skill_name,
            "path": str(repo_path),
            "skill_md": str(root_skill)
        })
        return skills
    
    # Check for skills/ subdirectory
    skills_subdir = repo_path / "skills"
    if skills_subdir.exists():
        for item in skills_subdir.iterdir():
            if item.is_dir() and (item / "SKILL.md").exists():
                skills.append({
                    "name": item.name,
                    "path": str(item),
                    "skill_md": str(item / "SKILL.md")
                })
    
    # Check root-level subdirectories
    for item in repo_path.iterdir():
        if item.is_dir() and not item.name.startswith(".") and (item / "SKILL.md").exists():
            skills.append({
                "name": item.name,
                "path": str(item),
                "skill_md": str(item / "SKILL.md")
            })
    
    return skills


def install_skill(skill_info: dict, versions: dict, force: bool = False) -> bool:
    """
    Install a skill into the Manus skills directory.
    Returns True if installed successfully.
    """
    skill_name = skill_info["name"]
    skill_src = Path(skill_info["path"])
    skill_dest = SKILLS_DIR / skill_name
    
    print(f"\n  [→] Installing skill: {skill_name}")
    
    # Check if already installed
    if skill_dest.exists() and not force:
        print(f"  [!] Skill '{skill_name}' already exists at {skill_dest}")
        response = input(f"      Overwrite? [y/N]: ").strip().lower()
        if response != "y":
            print(f"  [~] Skipped: {skill_name}")
            return False
    
    # Copy skill directory
    if skill_dest.exists():
        shutil.rmtree(skill_dest)
    
    shutil.copytree(skill_src, skill_dest, ignore=shutil.ignore_patterns(".git", "__pycache__", "*.pyc"))
    print(f"  [✓] Installed to {skill_dest}")
    
    # Read SKILL.md for description
    skill_md = skill_dest / "SKILL.md"
    if skill_md.exists():
        with open(skill_md, "r") as f:
            content = f.read()
        # Extract name from frontmatter if present
        if content.startswith("---"):
            lines = content.split("\n")
            for line in lines[1:]:
                if line.startswith("name:"):
                    skill_name = line.split(":", 1)[1].strip()
                    break
    
    return True


def print_skill_summary(skill_info: dict):
    """Print a summary of a discovered skill."""
    skill_md = Path(skill_info["skill_md"])
    if skill_md.exists():
        with open(skill_md, "r") as f:
            content = f.read()
        # Extract description from frontmatter
        description = "No description available"
        if content.startswith("---"):
            lines = content.split("\n")
            for line in lines[1:]:
                if line.startswith("description:"):
                    description = line.split(":", 1)[1].strip().strip('"')
                    break
        print(f"    Name:        {skill_info['name']}")
        print(f"    Description: {description[:100]}...")
    else:
        print(f"    Name: {skill_info['name']}")


def main():
    parser = argparse.ArgumentParser(
        description="Receive and install Manus skills from a GitHub repository.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python3 receive_skill.py tywade1980/manus-DRS-skills
  python3 receive_skill.py tywade1980/manus-DRS-skills --version v1.0.0
  python3 receive_skill.py tywade1980/manus-DRS-skills --skill caroline-ai
  python3 receive_skill.py tywade1980/manus-DRS-skills --list
        """
    )
    parser.add_argument("repo", help="GitHub repository in format owner/repo")
    parser.add_argument("--version", default=None, help="Git tag or branch to use (default: main)")
    parser.add_argument("--skill", default=None, help="Install only a specific skill by name")
    parser.add_argument("--list", action="store_true", help="List available skills without installing")
    parser.add_argument("--force", action="store_true", help="Overwrite existing skills without prompting")
    
    args = parser.parse_args()
    
    print(f"\n{'='*60}")
    print(f"  Manus Skill Receiver")
    print(f"  Repository: {args.repo}")
    if args.version:
        print(f"  Version:    {args.version}")
    print(f"{'='*60}\n")
    
    # Create temp directory for cloning
    with tempfile.TemporaryDirectory(prefix="manus_skill_") as tmp_dir:
        clone_target = os.path.join(tmp_dir, "repo")
        
        # Clone the repository
        if not clone_repo(args.repo, clone_target, args.version):
            print("\n[✗] Failed to clone repository. Exiting.")
            sys.exit(1)
        
        # Get version info
        repo_version = get_repo_version(clone_target)
        print(f"  [i] Repository version: {repo_version}")
        
        # Discover skills
        skills = discover_skills(clone_target)
        
        if not skills:
            print(f"\n[!] No skills found in {args.repo}.")
            print("    Expected: directories with SKILL.md files, or a root-level SKILL.md.")
            sys.exit(0)
        
        print(f"\n  [i] Found {len(skills)} skill(s):\n")
        for skill in skills:
            print_skill_summary(skill)
            print()
        
        if args.list:
            print("[i] List mode — no skills installed.")
            sys.exit(0)
        
        # Filter by skill name if specified
        if args.skill:
            skills = [s for s in skills if s["name"] == args.skill]
            if not skills:
                print(f"[✗] Skill '{args.skill}' not found in repository.")
                sys.exit(1)
        
        # Load existing versions
        versions = load_versions()
        
        # Install skills
        installed = []
        for skill_info in skills:
            success = install_skill(skill_info, versions, force=args.force)
            if success:
                installed.append(skill_info["name"])
                versions[skill_info["name"]] = repo_version
        
        # Save updated versions
        if installed:
            save_versions(versions)
        
        # Summary
        print(f"\n{'='*60}")
        print(f"  Installation Complete")
        print(f"  Installed: {len(installed)} skill(s)")
        for name in installed:
            print(f"    [✓] {name}")
        print(f"{'='*60}\n")


if __name__ == "__main__":
    main()
