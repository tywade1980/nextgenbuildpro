#!/usr/bin/env python3
"""
publish_skill.py - Publish a local Manus skill to a GitHub repository.

Usage:
    python3 publish_skill.py <skill_name> <github_repo> [--tag <version>] [--message <msg>]

Examples:
    python3 publish_skill.py caroline-ai tywade1980/manus-DRS-skills
    python3 publish_skill.py caroline-ai tywade1980/manus-DRS-skills --tag v1.0.0
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


def run_cmd(cmd: list, cwd: str = None, capture: bool = True) -> tuple[int, str, str]:
    """Run a shell command and return (returncode, stdout, stderr)."""
    result = subprocess.run(cmd, cwd=cwd, capture_output=capture, text=True)
    return result.returncode, result.stdout.strip(), result.stderr.strip()


def ensure_repo_exists(repo: str) -> bool:
    """Check if a GitHub repo exists, create it if not."""
    rc, out, err = run_cmd(["gh", "repo", "view", repo])
    if rc == 0:
        return True
    print(f"  [!] Repository {repo} not found. Creating ...")
    owner, name = repo.split("/", 1)
    rc, out, err = run_cmd(["gh", "repo", "create", repo, "--private", "--description", "Manus DRS Skills Repository"])
    if rc != 0:
        print(f"  [✗] Failed to create repo: {err}")
        return False
    print(f"  [✓] Created repository: {repo}")
    return True


def main():
    parser = argparse.ArgumentParser(
        description="Publish a local Manus skill to a GitHub repository."
    )
    parser.add_argument("skill_name", help="Name of the skill to publish (must exist in /home/ubuntu/skills/)")
    parser.add_argument("repo", help="GitHub repository in format owner/repo")
    parser.add_argument("--tag", default=None, help="Git tag for this version (e.g., v1.0.0)")
    parser.add_argument("--message", default=None, help="Commit message")
    
    args = parser.parse_args()
    
    skill_path = SKILLS_DIR / args.skill_name
    if not skill_path.exists():
        print(f"[✗] Skill '{args.skill_name}' not found at {skill_path}")
        sys.exit(1)
    
    skill_md = skill_path / "SKILL.md"
    if not skill_md.exists():
        print(f"[✗] No SKILL.md found in {skill_path}")
        sys.exit(1)
    
    print(f"\n{'='*60}")
    print(f"  Manus Skill Publisher")
    print(f"  Skill:      {args.skill_name}")
    print(f"  Repository: {args.repo}")
    print(f"{'='*60}\n")
    
    # Ensure repo exists
    if not ensure_repo_exists(args.repo):
        sys.exit(1)
    
    with tempfile.TemporaryDirectory(prefix="manus_publish_") as tmp_dir:
        clone_target = os.path.join(tmp_dir, "repo")
        
        # Clone the repo
        print(f"  [→] Cloning {args.repo} ...")
        rc, out, err = run_cmd(["gh", "repo", "clone", args.repo, clone_target])
        if rc != 0:
            print(f"  [✗] Clone failed: {err}")
            sys.exit(1)
        
        # Copy skill into repo under skills/<skill_name>/
        dest_skills_dir = Path(clone_target) / "skills"
        dest_skills_dir.mkdir(exist_ok=True)
        dest_skill = dest_skills_dir / args.skill_name
        
        if dest_skill.exists():
            shutil.rmtree(dest_skill)
        
        shutil.copytree(skill_path, dest_skill, ignore=shutil.ignore_patterns(".git", "__pycache__", "*.pyc"))
        print(f"  [✓] Copied skill to {dest_skill}")
        
        # Update README
        readme_path = Path(clone_target) / "README.md"
        skills_in_repo = [d.name for d in dest_skills_dir.iterdir() if d.is_dir() and (d / "SKILL.md").exists()]
        readme_content = f"# Manus DRS Skills\n\nShared skills for the Wade Ecosystem.\n\n## Available Skills\n\n"
        for s in sorted(skills_in_repo):
            readme_content += f"- `{s}`\n"
        readme_content += f"\n## Usage\n\n```bash\npython3 /home/ubuntu/skills/skill-share/scripts/receive_skill.py tywade1980/manus-DRS-skills\n```\n"
        with open(readme_path, "w") as f:
            f.write(readme_content)
        
        # Git commit and push
        commit_msg = args.message or f"Add/update skill: {args.skill_name}"
        
        run_cmd(["git", "config", "user.email", "manus@wade-ecosystem.ai"], cwd=clone_target)
        run_cmd(["git", "config", "user.name", "Manus Agent"], cwd=clone_target)
        run_cmd(["git", "add", "."], cwd=clone_target)
        
        rc, out, err = run_cmd(["git", "commit", "-m", commit_msg], cwd=clone_target)
        if rc != 0 and "nothing to commit" not in err:
            print(f"  [✗] Commit failed: {err}")
            sys.exit(1)
        
        rc, out, err = run_cmd(["git", "push"], cwd=clone_target)
        if rc != 0:
            print(f"  [✗] Push failed: {err}")
            sys.exit(1)
        
        print(f"  [✓] Pushed to {args.repo}")
        
        # Tag if requested
        if args.tag:
            run_cmd(["git", "tag", args.tag], cwd=clone_target)
            rc, out, err = run_cmd(["git", "push", "origin", args.tag], cwd=clone_target)
            if rc == 0:
                print(f"  [✓] Tagged as {args.tag}")
        
        print(f"\n{'='*60}")
        print(f"  Published: {args.skill_name} → {args.repo}")
        print(f"{'='*60}\n")


if __name__ == "__main__":
    main()
